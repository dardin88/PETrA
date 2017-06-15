package it.unisa.petra.core;

import it.unisa.petra.core.batterystats.BatteryStatsParser;
import it.unisa.petra.core.batterystats.EnergyInfo;
import it.unisa.petra.core.exceptions.ADBNotFoundException;
import it.unisa.petra.core.exceptions.AppNameCannotBeExtractedException;
import it.unisa.petra.core.exceptions.NoDeviceFoundException;
import it.unisa.petra.core.powerprofile.PowerProfile;
import it.unisa.petra.core.powerprofile.PowerProfileParser;
import it.unisa.petra.core.systrace.CpuFrequency;
import it.unisa.petra.core.systrace.SysTrace;
import it.unisa.petra.core.systrace.SysTraceParser;
import it.unisa.petra.core.traceview.TraceLine;
import it.unisa.petra.core.traceview.TraceViewParser;
import it.unisa.petra.core.traceview.TraceviewStructure;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Process {

    public void installApp(String apkLocation) throws NoDeviceFoundException, ADBNotFoundException {

        this.checkADBExists();

        this.executeCommand("adb shell dumpsys battery set ac 0", null);
        this.executeCommand("adb shell dumpsys battery set usb 0", null);

        System.out.println("Installing app.");
        this.executeCommand("adb install " + apkLocation, null);
    }

    public void uninstallApp(String appName) throws NoDeviceFoundException, ADBNotFoundException {

        this.checkADBExists();

        System.out.println("Uninstalling app.");
        this.executeCommand("adb shell pm uninstall " + appName, null);
    }

    public ProcessOutput playRun(int run, String appName, int interactions, int timeBetweenInteractions,
                                 int timeCapturing, String scriptLocationPath, String powerProfileFile, String outputLocation)
            throws InterruptedException, IOException, NoDeviceFoundException, ADBNotFoundException {

        String sdkFolderPath = System.getenv("ANDROID_HOME");
        this.checkADBExists();

        String platformToolsFolder = sdkFolderPath + File.separator + "platform-tools";
        String toolsFolder = sdkFolderPath + File.separator + "tools";

        Random random = new Random();
        int seed = random.nextInt();

        if (scriptLocationPath.isEmpty()) {
            System.out.println("Run " + run + ": seed: " + seed);
        }
        String runDataFolderName = outputLocation + "/run_" + run + File.separator;
        File runDataFolder = new File(runDataFolderName);

        runDataFolder.mkdirs();

        String batteryStatsFilename = runDataFolderName + "batterystats";
        String systraceFilename = runDataFolderName + "systrace";
        String traceviewFilename = runDataFolderName + "tracedump";

        this.resetApp(appName, run);
        this.startApp(appName);

        Date time1 = new Date();
        SysTraceRunner sysTraceRunner = this.startProfiling(appName, run, timeCapturing, systraceFilename, platformToolsFolder);
        Thread systraceThread = new Thread(sysTraceRunner);
        systraceThread.start();

        this.executeActions(appName, run, scriptLocationPath, toolsFolder, interactions, timeBetweenInteractions, seed);

        Date time2 = new Date();
        long timespent = time2.getTime() - time1.getTime();

        timeCapturing = (int) ((timespent + 10000) / 1000);

        this.extractInfo(appName, run, batteryStatsFilename, runDataFolderName, platformToolsFolder, traceviewFilename);

        systraceThread.join();

        System.out.println("Run " + run + ": aggregating results.");

        System.out.println("Run " + run + ": parsing power profile.");
        PowerProfile powerProfile = PowerProfileParser.parseFile(powerProfileFile);

        List<TraceLine> traceLinesWiConsumptions = parseAndAggregateResults(traceviewFilename, batteryStatsFilename,
                systraceFilename, powerProfile, appName, run);

        PrintWriter resultsWriter = new PrintWriter(runDataFolderName + "result.csv", "UTF-8");
        resultsWriter.println("signature, joule, seconds");

        for (TraceLine traceLine : traceLinesWiConsumptions) {
            resultsWriter.println(traceLine.getSignature() + "," + traceLine.getConsumption() + "," + traceLine.getTimeLength());
        }

        resultsWriter.flush();
        this.stopApp(appName, run);

        this.executeCommand("adb shell dumpsys battery reset", null);

        System.out.println("Run " + run + ": complete.");
        return new ProcessOutput(timeCapturing, seed);
    }

    public void extractPowerProfile(String outputLocation) throws NoDeviceFoundException {
        System.out.println("Extracting power profile.");
        String jarDirectory = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();

        this.executeCommand("adb pull /system/framework/framework-res.apk", null);

        this.executeCommand("jar xf " + jarDirectory + "/PETrA.jar apktool_2.2.2.jar", null);
        this.executeCommand("java -jar apktool_2.2.2.jar if framework-res.apk", null);
        this.executeCommand("java -jar apktool_2.2.2.jar d framework-res.apk", null);
        this.executeCommand("mv " + jarDirectory + "/framework-res/res/xml/power_profile.xml " + outputLocation, null);
        this.executeCommand("rm -rf " + jarDirectory + "/apktool_2.2.2.jar", null);
        this.executeCommand("rm -rf " + jarDirectory + "/framework-res.apk", null);
        this.executeCommand("rm -rf " + jarDirectory + "/framework-res", null);
    }

    private void resetApp(String appName, int run) throws NoDeviceFoundException {
        System.out.println("Run " + run + ": resetting app and batteristats.");
        this.executeCommand("adb shell pm clear " + appName, null);
        this.executeCommand("adb shell dumpsys batterystats --reset", null);
    }

    private SysTraceRunner startProfiling(String appName, int run, int timeCapturing, String systraceFilename,
                                          String platformToolsFolder) throws NoDeviceFoundException {
        System.out.println("Run " + run + ": starting profiling.");
        this.executeCommand("adb shell am profile start " + appName + " ./data/local/tmp/log.trace", null);

        System.out.println("Run " + run + ": capturing system traces.");
        return new SysTraceRunner(timeCapturing, systraceFilename, platformToolsFolder);
    }

    private void executeActions(String appName, int run, String scriptLocationPath, String toolsFolder, int interactions,
                                int timeBetweenInteractions, int seed) throws NoDeviceFoundException {
        if (scriptLocationPath.isEmpty()) {
            System.out.println("Run " + run + ": executing random actions.");
            this.executeCommand("adb shell monkey -p " + appName + " -s " + seed + " --throttle " + timeBetweenInteractions + " --ignore-crashes --ignore-timeouts --ignore-security-exceptions " + interactions, null);
        } else {
            System.out.println("Run " + run + ": running monkeyrunner script.");
            String jarDirectory = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
            this.executeCommand("jar xf " + jarDirectory + "/PETrA.jar monkey_playback.py", null);
            this.executeCommand(toolsFolder + "/bin/monkeyrunner " + jarDirectory + "/monkey_playback.py " + scriptLocationPath, null);
            this.executeCommand("rm -rf " + jarDirectory + "/monkey_playback.py", null);
        }
    }

    private void extractInfo(String appName, int run, String batteryStatsFilename, String runDataFolderName, String platformToolsFolder, String traceviewFilename) throws NoDeviceFoundException {
        System.out.println("Run " + run + ": stop profiling.");
        this.executeCommand("adb shell am profile stop " + appName, null);

        System.out.println("Run " + run + ": saving battery stats.");
        this.executeCommand("adb shell dumpsys batterystats", new File(batteryStatsFilename));

        System.out.println("Run " + run + ": saving traceviews.");
        this.executeCommand("adb pull ./data/local/tmp/log.trace " + runDataFolderName, null);
        this.executeCommand(platformToolsFolder + "/dmtracedump -o " + runDataFolderName + "log.trace", new File(traceviewFilename));

    }

    List<TraceLine> parseAndAggregateResults(String traceviewFilename, String batteryStatsFilename, String systraceFilename,
                                             PowerProfile powerProfile, String appName, int run) throws IOException {
        List<TraceLine> traceLinesWConsumption = new ArrayList();

        System.out.println("Run " + run + ": elaborating traceview info.");
        TraceviewStructure traceviewStructure = TraceViewParser.parseFile(traceviewFilename, appName);
        List<TraceLine> traceLines = traceviewStructure.getTraceLines();
        int traceviewLength = traceviewStructure.getEndTime();
        int traceviewStart = traceviewStructure.getStartTime();

        System.out.println("Run " + run + ": elaborating battery stats info.");
        List<EnergyInfo> energyInfoArray = BatteryStatsParser.parseFile(batteryStatsFilename, traceviewStart, traceviewLength);

        System.out.println("Run " + run + ": elaborating systrace stats info.");
        SysTrace cpuInfo = SysTraceParser.parseFile(systraceFilename, traceviewStart, traceviewLength);

        System.out.println("Run " + run + ": aggregating results.");
        energyInfoArray = this.mergeEnergyInfo(energyInfoArray, cpuInfo, cpuInfo.getNumberOfCpu());
        for (TraceLine traceLine : traceLines) {
            traceLinesWConsumption.add(this.calculateConsumption(traceLine, energyInfoArray, powerProfile));
        }

        return traceLinesWConsumption;
    }

    private List<EnergyInfo> mergeEnergyInfo(List<EnergyInfo> energyInfoArray, SysTrace cpuInfo, int numOfCore) {

        List<Integer> cpuFrequencies = new ArrayList<>();

        for (int i = 0; i < numOfCore; i++) {
            cpuFrequencies.add(0);
        }

        for (EnergyInfo energyInfo : energyInfoArray) {
            int fixedEnergyInfoTime = cpuInfo.getSystraceStartTime() + energyInfo.getTime() * 1000; //systrace time are in nanoseconds
            for (CpuFrequency frequency : cpuInfo.getFrequencies()) {
                if (frequency.getTime() <= fixedEnergyInfoTime) {
                    cpuFrequencies.set(frequency.getCore(), frequency.getValue());
                }
            }
            energyInfo.setCpuFrequencies(cpuFrequencies);
        }
        return energyInfoArray;
    }

    private TraceLine calculateConsumption(TraceLine traceLine, List<EnergyInfo> energyInfoArray, PowerProfile powerProfile) {

        double joule = 0;
        double totalSeconds = 0;

        int numberOfCores = energyInfoArray.get(0).getCpuFrequencies().size();

        boolean[] previouslyIdle = new boolean[numberOfCores];

        for (EnergyInfo energyInfo : energyInfoArray) {

            double ampere = 0;

            List<Integer> cpuFrequencies = energyInfo.getCpuFrequencies();

            for (int i = 0; i < numberOfCores; i++) {
                int coreFrequency = cpuFrequencies.get(i);
                int coreCluster = powerProfile.getClusterByCore(i);
                ampere += powerProfile.getCpuConsumptionByFrequency(coreCluster, coreFrequency) / 1000;
                if (coreFrequency != 0) {
                    if (previouslyIdle[i]) {
                        ampere += powerProfile.getDevices().get("cpu.awake") / 1000;
                    }
                } else {
                    previouslyIdle[i] = true;
                }
            }

            for (String deviceString : energyInfo.getDevices()) {
                if (deviceString.contains("wifi")) {
                    ampere += powerProfile.getDevices().get("wifi.on") / 1000;
                } else if (deviceString.contains("wifi.scanning")) {
                    ampere += powerProfile.getDevices().get("wifi.scan") / 1000;
                } else if (deviceString.contains("wifi.running")) {
                    ampere += powerProfile.getDevices().get("wifi.active") / 1000;
                } else if (deviceString.contains("phone.scanning")) {
                    ampere += powerProfile.getDevices().get("radio.scan") / 1000;
                } else if (deviceString.contains("phone.running")) {
                    ampere += powerProfile.getDevices().get("radio.active") / 1000;
                } else if (deviceString.contains("bluetooth")) {
                    ampere += powerProfile.getDevices().get("bluetooth.on") / 1000;
                } else if (deviceString.contains("bluetooth.running")) {
                    ampere += powerProfile.getDevices().get("bluetooth.active") / 1000;
                } else if (deviceString.contains("screen")) {
                    ampere += powerProfile.getDevices().get("screen.on") / 1000;
                } else if (deviceString.contains("gps")) {
                    ampere += powerProfile.getDevices().get("gps.on") / 1000;
                }
            }

            int phoneSignalStrength = energyInfo.getPhoneSignalStrength();

            if (powerProfile.getRadioInfo().size() == phoneSignalStrength - 1) {
                ampere += powerProfile.getRadioInfo().get(phoneSignalStrength - 1) / 1000;
            } else {
                ampere += powerProfile.getRadioInfo().get(powerProfile.getRadioInfo().size() - 1) / 1000;
            }


            double watt = ampere * energyInfo.getVoltage() / 1000;
            double microseconds = 0;
            if (traceLine.getEntrance() >= energyInfo.getTime()) {
                if (traceLine.getEntrance() > energyInfo.getTime()) {
                    microseconds = traceLine.getExit() - energyInfo.getTime();
                } else {
                    microseconds = traceLine.getExit() - traceLine.getEntrance();
                }
            }
            double seconds = microseconds / 1000000000;
            totalSeconds += seconds;
            joule += watt * seconds;
        }

        traceLine.setTimeLength(totalSeconds);
        traceLine.setConsumption(joule);

        return traceLine;
    }

    private void startApp(String appName) throws NoDeviceFoundException {
        System.out.println("Starting app.");
        this.executeCommand("adb shell input keyevent 82", null);
        this.executeCommand("adb shell monkey -p " + appName + " 1", null);
        this.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable true", null);

    }

    private void stopApp(String appName, int run) throws NoDeviceFoundException {
        System.out.println("Run " + run + ": stopping app.");
        this.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable false", null);
        this.executeCommand("adb shell am force-stop " + appName, null);
        this.executeCommand("adb shell pm clear " + appName, null);
    }

    private String executeCommand(String command, File outputFile) throws NoDeviceFoundException {

        StringBuilder output = new StringBuilder();

        try {
            List<String> listCommands = new ArrayList<>();

            String[] arrayExplodedCommands = command.split(" ");
            listCommands.addAll(Arrays.asList(arrayExplodedCommands));
            ProcessBuilder pb = new ProcessBuilder(listCommands);
            pb.redirectErrorStream(true);
            if (outputFile != null) {
                pb.redirectOutput(outputFile);
            }

            java.lang.Process commandProcess = pb.start();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(commandProcess.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    output.append(line).append("\n");
                    if (line.contains("error: no devices/emulators found")) {
                        throw new NoDeviceFoundException();
                    }
                }

                commandProcess.waitFor();
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output.toString();
    }

    private void checkADBExists() throws ADBNotFoundException {
        String sdkFolderPath = System.getenv("ANDROID_HOME");
        String adbPath = sdkFolderPath + "/platform-tools/adb";
        File adbFile = new File(adbPath);
        if (!adbFile.exists()) {
            throw new ADBNotFoundException();
        }
    }

    public String extractAppName(String apkLocationPath) throws NoDeviceFoundException, AppNameCannotBeExtractedException {
        String sdkFolderPath = System.getenv("ANDROID_HOME");
        String aaptPath = sdkFolderPath + "/build-tools/25.0.3/aapt";
        String aaptOutput = this.executeCommand(aaptPath + " dump badging " + apkLocationPath, null);
        String appName = "";
        Pattern pattern = Pattern.compile("package: name='([^']*)' versionCode='[^']*' versionName='[^']*' platformBuildVersionName='[^']*'");

        for (String line : aaptOutput.split("\n")) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                appName = matcher.group(1);
            }
        }

        if (appName.isEmpty()) {
            throw new AppNameCannotBeExtractedException();
        }

        return appName;
    }
}
