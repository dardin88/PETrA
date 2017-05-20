package it.unisa.petra.core;

import it.unisa.petra.core.batterystats.BatteryStatsParser;
import it.unisa.petra.core.batterystats.EnergyInfo;
import it.unisa.petra.core.exceptions.NoDeviceFoundException;
import it.unisa.petra.core.powerprofile.PowerProfile;
import it.unisa.petra.core.powerprofile.PowerProfileParser;
import it.unisa.petra.core.systrace.CpuFreq;
import it.unisa.petra.core.systrace.SysTrace;
import it.unisa.petra.core.systrace.SysTraceParser;
import it.unisa.petra.core.systrace.SysTraceRunner;
import it.unisa.petra.core.traceview.TraceLine;
import it.unisa.petra.core.traceview.TraceViewParser;
import it.unisa.petra.core.traceview.TraceviewStructure;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Process {

    public void installApp(String apkLocation) throws NoDeviceFoundException {
        this.executeCommand("adb shell dumpsys battery set ac 0", null);
        this.executeCommand("adb shell dumpsys battery set usb 0", null);

        System.out.println("Installing app.");
        this.executeCommand("adb install " + apkLocation, null);
    }

    public void uninstallApp(String appName) throws NoDeviceFoundException {
        System.out.println("Uninstalling app.");
        this.executeCommand("adb shell pm uninstall " + appName, null);
    }

    public ProcessOutput playRun(int run, String appName, int interactions, int timeBetweenInteractions,
                                 int timeCapturing, String scriptLocationPath, String sdkFolderPath, String powerProfilePath, String outputLocation)
            throws InterruptedException, IOException, NoDeviceFoundException {

        String runString = "Run " + run + ": ";

        File platformToolsFolder = new File(sdkFolderPath + File.separator + "platform-tools");
        File toolsFolder = new File(sdkFolderPath + File.separator + "tools");

        Random random = new Random();
        String seed = random.nextInt() + "";

        if (scriptLocationPath.isEmpty()) {
            System.out.println(runString + "seed: " + seed);
        }
        String runDataFolderName = outputLocation + "run_" + run + File.separator;
        File runDataFolder = new File(runDataFolderName);

        runDataFolder.mkdirs();


        String batteryStatsFilename = runDataFolderName + "batterystats";
        String systraceFilename = runDataFolderName + "systrace";
        String traceviewFilename = runDataFolderName + "tracedump";

        this.executeCommand("adb shell pm clear " + appName, null);

        System.out.println(runString + "resetting battery stats.");
        this.executeCommand("adb shell dumpsys batterystats --reset", null);

        System.out.println(runString + "opening app.");
        this.executeCommand("adb shell input keyevent 82", null);
        this.executeCommand("adb shell monkey -p " + appName + " 1", null);
        this.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable true", null);

        System.out.println(runString + "start profiling.");
        this.executeCommand("adb shell am profile start " + appName + " ./data/local/tmp/log.trace", null);
        Date time1 = new Date();

        System.out.println(runString + "capturing system traces.");
        SysTraceRunner sysTraceRunner = new SysTraceRunner(timeCapturing, systraceFilename, platformToolsFolder);
        Thread systraceThread = new Thread(sysTraceRunner);
        systraceThread.start();

        if (scriptLocationPath.isEmpty()) {
            System.out.println(runString + "executing random actions.");
        } else {
            System.out.println(runString + "running monkeyrunner script.");
        }
        this.executeCommand("adb kill-server", null);
        this.executeCommand("adb start-server", null);
        if (interactions > 0) {
            this.executeCommand("adb shell monkey -p " + appName + " -s " + seed + " --throttle " + timeBetweenInteractions + " --ignore-crashes --ignore-timeouts --ignore-security-exceptions " + interactions, null);
        } else {
            this.executeCommand(toolsFolder + "/bin/monkeyrunner " + toolsFolder + "monkey_playback.py " + scriptLocationPath, null);
        }

        Date time2 = new Date();
        long timespent = time2.getTime() - time1.getTime();

        timeCapturing = (int) ((timespent + 10000) / 1000);

        System.out.println(runString + "stop profiling.");
        this.executeCommand("adb shell am profile stop " + appName, null);

        System.out.println(runString + "saving battery stats.");
        this.executeCommand("adb shell dumpsys batterystats", new File(batteryStatsFilename));

        System.out.println(runString + "saving traceviews.");
        this.executeCommand("adb pull ./data/local/tmp/log.trace " + runDataFolderName, null);
        this.executeCommand(platformToolsFolder + "/dmtracedump -o " + runDataFolderName + "log.trace", new File(traceviewFilename));

        systraceThread.join();

        System.out.println(runString + "loading power profile.");
        PowerProfile powerProfile = null;
        try {
            powerProfile = PowerProfileParser.parseFile(powerProfilePath);
        } catch (IOException ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(runString + "elaborating traceview info.");
        try {
            TraceviewStructure traceviewStructure = TraceViewParser.parseFile(traceviewFilename, appName);
            List<TraceLine> traceLines = traceviewStructure.getTraceLines();
            int traceviewLength = traceviewStructure.getEndTime();
            int traceviewStart = traceviewStructure.getStartTime();

            System.out.println(runString + "elaborating battery stats info.");
            List<EnergyInfo> energyInfoArray = BatteryStatsParser.parseFile(batteryStatsFilename, traceviewStart, traceviewLength);

            System.out.println(runString + "elaborating sys trace stats info.");
            SysTrace cpuInfo = SysTraceParser.parseFile(systraceFilename, traceviewStart, traceviewLength);

            System.out.println(runString + "aggregating results.");

            PrintWriter resultsWriter = new PrintWriter(runDataFolderName + "result.csv", "UTF-8");
            resultsWriter.println("signature, joule, seconds");
            energyInfoArray = this.mergeEnergyInfo(energyInfoArray, cpuInfo);
            for (TraceLine traceLine : traceLines) {
                List result = this.calculateConsumption(traceLine.getEntrance(), traceLine.getExit(), energyInfoArray, powerProfile);
                resultsWriter.println(traceLine.getSignature() + "," + result.get(0) + "," + result.get(1));
            }
            resultsWriter.flush();
        } finally {
            System.out.println(runString + "stop app.");
            this.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable false", null);
            this.executeCommand("adb shell am force-stop " + appName, null);
            this.executeCommand("adb shell pm clear " + appName, null);
        }

        this.executeCommand("adb shell dumpsys battery reset", null);

        System.out.println(runString + "complete.");
        return new ProcessOutput(timeCapturing, seed);
    }

    private List<EnergyInfo> mergeEnergyInfo(List<EnergyInfo> energyInfoArray, SysTrace cpuInfo) {
        for (EnergyInfo energyInfo : energyInfoArray) {
            int fixedEnergyInfoTime = cpuInfo.getSystraceStartTime() + energyInfo.getTime() * 1000; //systrace time are in nanoseconds
            for (CpuFreq freq : cpuInfo.getFrequency()) {
                if (freq.getTime() <= fixedEnergyInfoTime) {
                    energyInfo.setCpuFreq(freq.getValue());
                }
            }
        }
        return energyInfoArray;
    }

    private List calculateConsumption(int timeEnter, int timeExit, List<EnergyInfo> energyInfoArray, PowerProfile powerProfile) {

        double joule = 0;
        double totalSeconds = 0;

        for (EnergyInfo energyInfo : energyInfoArray) {
            int cpuFrequency = energyInfo.getCpuFreq();
            double ampere = powerProfile.getCpuInfo().get(cpuFrequency) / 1000;
            for (String deviceString : energyInfo.getDevices()) {
                if (deviceString.contains("wifi")) {
                    ampere += powerProfile.getDevices().get("wifi.on") / 1000;
                } else if (deviceString.contains("screen")) {
                    ampere += powerProfile.getDevices().get("screen.on") / 1000;
                } else if (deviceString.contains("gps")) {
                    ampere += powerProfile.getDevices().get("gps.on") / 1000;
                }
            }
            double watt = ampere * energyInfo.getVoltage() / 1000;
            double microseconds = 0;
            if (timeEnter >= energyInfo.getTime()) {
                if (timeEnter > energyInfo.getTime()) {
                    microseconds = timeExit - energyInfo.getTime();
                } else {
                    microseconds = timeExit - timeEnter;
                }
            }
            double seconds = microseconds / 1000000000;
            totalSeconds += seconds;
            joule += watt * seconds;
        }

        ArrayList<Double> result = new ArrayList<>();
        result.add(joule);
        result.add(totalSeconds);
        return result;
    }

    private void executeCommand(String command, File outputFile) throws NoDeviceFoundException {
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
                    if (line.contains("error: no devices/emulators found")) {
                        throw new NoDeviceFoundException();
                    }
                }

                commandProcess.waitFor();
                Thread.sleep(3000);
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
