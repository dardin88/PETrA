package it.unisa.petra;

import it.unisa.petra.BatteryStats.BatteryStatsParser;
import it.unisa.petra.BatteryStats.EnergyInfo;
import it.unisa.petra.PowerProfile.PowerProfile;
import it.unisa.petra.PowerProfile.PowerProfileParser;
import it.unisa.petra.SysTrace.CpuFreq;
import it.unisa.petra.SysTrace.SysTrace;
import it.unisa.petra.SysTrace.SysTraceParser;
import it.unisa.petra.SysTrace.SysTraceRunner;
import it.unisa.petra.Traceview.TraceLine;
import it.unisa.petra.Traceview.TraceViewParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PETrAProcess {

    public void installApp(String outputLocation, String apkLocation) throws NoDeviceFoundException {
        this.executeCommand("adb shell dumpsys battery set usb 0", null, null, true);

        System.out.println("Installing app.");
        this.executeCommand("adb install " + apkLocation, null, null, true);
    }

    public void uninstallApp(String appName) throws NoDeviceFoundException {
        System.out.println("Uninstalling app.");
        this.executeCommand("adb shell pm uninstall " + appName, null, null, true);
    }

    public PETrAProcessOutput playRun(int run, int trials, String appName, int interactions, int timeBetweenInteractions,
            int timeCapturing, String platformToolsFolderPath, String powerProfileName, String outputLocation)
            throws InterruptedException, IOException, ParseException, NoDeviceFoundException {

        String runString = "Run " + run + ": ";

        File platformToolsFolder = new File(platformToolsFolderPath);

        Random random = new Random();
        String seed = random.nextInt() + "";

        System.out.println(runString + "seed: " + seed);
        String runDataFolderName = outputLocation + "run_" + run + "/";
        File runDataFolder = new File(runDataFolderName);
        runDataFolder.mkdirs();

        String batteryStatsFilename = runDataFolderName + "batterystats";
        String systraceFilename = runDataFolderName + "systrace";
        String traceviewFilename = runDataFolderName + "tracedump";

        this.executeCommand("adb shell pm clear " + appName, null, null, true);

        System.out.println(runString + "resetting battery stats.");
        this.executeCommand("adb shell dumpsys batterystats --reset", null, null, true);

        System.out.println(runString + "opening app.");
        this.executeCommand("adb shell input keyevent 82", null, null, true);
        this.executeCommand("adb shell monkey -p " + appName + " 1", null, null, true);
        this.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable true", null, null, true);

        System.out.println(runString + "start profiling.");
        this.executeCommand("adb shell am profile start " + appName + " ./data/local/tmp/log.trace", null, null, true);
        Date time1 = new Date();

        System.out.println(runString + "capturing system traces.");
        SysTraceRunner sysTraceRunner = new SysTraceRunner(timeCapturing, systraceFilename, platformToolsFolder);
        Thread systraceThread = new Thread(sysTraceRunner);
        systraceThread.start();

        System.out.println(runString + "executing random actions.");
        this.executeCommand("adb kill-server", null, null, true);
        this.executeCommand("adb start-server", null, null, true);
        this.executeCommand("adb shell monkey -p " + appName + " -s " + seed + " --throttle " + timeBetweenInteractions + " --ignore-crashes --ignore-timeouts --ignore-security-exceptions " + interactions, null, null, true);

        Date time2 = new Date();
        long timespent = time2.getTime() - time1.getTime();

        timeCapturing = (int) ((timespent + 10000) / 1000);

        System.out.println(runString + "stop profiling.");
        this.executeCommand("adb shell am profile stop " + appName, null, null, true);

        System.out.println(runString + "saving battery stats.");
        this.executeCommand("adb shell dumpsys batterystats", null, new File(batteryStatsFilename), true);

        System.out.println(runString + "saving traceviews.");
        this.executeCommand("adb pull ./data/local/tmp/log.trace " + runDataFolderName, null, null, true);
        this.executeCommand("./dmtracedump -o " + runDataFolderName + "log.trace", platformToolsFolder, new File(traceviewFilename), true);

        systraceThread.join();

        System.out.println(runString + "loading power profile.");
        PowerProfile powerProfile = null;
        try {
            powerProfile = PowerProfileParser.parseFile(powerProfileName);
        } catch (IOException ex) {
            Logger.getLogger(PETrAProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(runString + "elaborating traceview info.");
        List<TraceLine> traceLines;
        try {
            traceLines = TraceViewParser.parseFile(traceviewFilename, appName, runDataFolderName);
            int traceviewLength = traceLines.get(0).setTimeLength();
            int traceviewStart = traceLines.get(0).getTraceViewStartingTime();

            System.out.println(runString + "elaborating battery stats info.");
            List<EnergyInfo> energyInfoArray = BatteryStatsParser.parseFile(batteryStatsFilename, traceviewStart, traceviewLength, appName);

            System.out.println(runString + "elaborating sys trace stats info.");
            SysTrace cpuInfo = SysTraceParser.main(systraceFilename, traceviewStart, traceviewLength);

            System.out.println(runString + "aggregating results.");

            PrintWriter resultsWriter = new PrintWriter(runDataFolderName + "result.csv", "UTF-8");
            resultsWriter.println("signature, joule, seconds");
            energyInfoArray = this.mergeEnergyInfo(energyInfoArray, cpuInfo, powerProfile);
            for (TraceLine traceLine : traceLines) {
                List<Double> result = this.calculateConsumption(traceLine.getEntrance(), traceLine.getExit(), energyInfoArray, powerProfile);
                resultsWriter.println(traceLine.getSignature() + "," + result.get(0) + "," + result.get(1));
            }
            resultsWriter.flush();
        } finally {
            System.out.println(runString + "stop app.");
            this.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable false", null, null, true);
            this.executeCommand("adb shell am force-stop " + appName, null, null, true);
            this.executeCommand("adb shell pm clear " + appName, null, null, true);
        }

        this.executeCommand("adb shell dumpsys battery reset", null, null, true);

        System.out.println(runString + "complete.");
        return new PETrAProcessOutput(timeCapturing, seed);
    }

    private List<EnergyInfo> mergeEnergyInfo(List<EnergyInfo> energyInfoArray, SysTrace cpuInfo, PowerProfile powerProfile) throws IOException, ParseException {
        for (EnergyInfo energyInfo : energyInfoArray) {
            int fixedEnergyInfoTime = cpuInfo.getSystraceStartTime() + energyInfo.getTime();
            for (CpuFreq freq : cpuInfo.getFrequency()) {
                if (freq.getTime() <= fixedEnergyInfoTime) {
                    energyInfo.setCpuFreq(freq.getValue());
                }
            }
        }
        return energyInfoArray;
    }

    private List calculateConsumption(int timeEnter, int timeExit, List<EnergyInfo> energyInfoArray, PowerProfile powerProfile) throws IOException, ParseException {

        double joul = 0;
        double totalSeconds = 0;

        for (int i = 0; i < energyInfoArray.size(); i++) {
            int cpuFrequency = energyInfoArray.get(i).getCpuFreq();
            double ampere = (double) powerProfile.getCpuInfo().get(cpuFrequency) / 1000;
            for (String deviceString : energyInfoArray.get(i).getDevices()) {
                if (deviceString.contains("wifi")) {
                    ampere += powerProfile.getDevices().get("wifi.on") / 1000;
                } else if (deviceString.contains("screen")) {
                    ampere += powerProfile.getDevices().get("screen.on") / 1000;
                } else if (deviceString.contains("gps")) {
                    ampere += powerProfile.getDevices().get("gps.on") / 1000;
                }
            }
            double watt = ampere * energyInfoArray.get(i).getVoltage() / 1000;
            double microseconds = 0;
            if (timeEnter >= energyInfoArray.get(i).getTime()) {
                if (timeEnter > energyInfoArray.get(i).getTime()) {
                    microseconds = timeExit - energyInfoArray.get(i).getTime();
                } else {
                    microseconds = timeExit - timeEnter;
                }
            }
            double seconds = microseconds / 1000000;
            totalSeconds += seconds;
            joul += watt * seconds;
        }

        ArrayList<Double> result = new ArrayList<>();
        result.add(joul);
        result.add(totalSeconds);
        return result;
    }

    private void executeCommand(String command, File directoryFolder, File outputFile, boolean waitfor) throws NoDeviceFoundException {
        try {
            List<String> listCommands = new ArrayList<>();

            String[] arrayExplodedCommands = command.split(" ");
            listCommands.addAll(Arrays.asList(arrayExplodedCommands));
            ProcessBuilder pb = new ProcessBuilder(listCommands);
            if (directoryFolder != null) {
                pb.directory(directoryFolder);
            }
            pb.redirectErrorStream(true);
            if (outputFile != null) {
                pb.redirectOutput(outputFile);
            }

            Process commandProcess = pb.start();

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
            Logger.getLogger(PETrAProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
