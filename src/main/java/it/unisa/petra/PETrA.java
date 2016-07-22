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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PETrA {

    public static void main(String[] args) throws InterruptedException, ParseException {

        ArrayList<String> appNames = new ArrayList<>();
        ArrayList<String> apkNames = new ArrayList<>();

        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/home/dardin88/Desktop/energy_consumption_bad_smell/icse_experiment/app_list.csv"));
            while ((line = br.readLine()) != null) {
                appNames.add(line);
                apkNames.add(line + ".apk");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PETrA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PETrA.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int appCounter = 0; appCounter < appNames.size(); appCounter++) {

            String appName = appNames.get(appCounter);
            String apkName = apkNames.get(appCounter);
            String outputLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/icse_experiment/test_data/" + appName + "/";
            String apkLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/icse_experiment/apks/" + apkName;
            String powerProfileName = null;
            File platformToolsFolder = null;
            int maxRun = 0;
            int interactions = 0;
            int timeBetweenInteractions = 0;

            try {
                powerProfileName = ConfigManager.getPowerProfileFile();
                platformToolsFolder = new File(ConfigManager.getPlatformToolsFolder());
                maxRun = ConfigManager.getMaxRun();
                interactions = ConfigManager.getInteractions();
                timeBetweenInteractions = ConfigManager.getTimeBetweenInteractions();
            } catch (IOException ex) {
                Logger.getLogger(PETrA.class.getName()).log(Level.SEVERE, null, ex);
            }

            File appDataFolder = new File(outputLocation);

            int timeCapturing = (interactions * timeBetweenInteractions) / 1000;

            appDataFolder.mkdirs();

            File seedsFile = new File(outputLocation + "seeds");
            BufferedWriter seedsWriter = null;
            try {
                seedsWriter = new BufferedWriter(new FileWriter(seedsFile, true));
            } catch (IOException ex) {
                Logger.getLogger(PETrA.class.getName()).log(Level.SEVERE, null, ex);
            }

            PETrA.executeCommand("adb shell dumpsys battery set usb 0", null, null, true);

            System.out.println("Installing app.");
            PETrA.executeCommand("adb install " + apkLocation, null, null, true);

            for (int run = 0; run < maxRun; run++) {
                System.out.println("==========================RUN_" + run + "======================================");

                int trials = 0;

                String runDataFolderName = outputLocation + "run_" + run + "/";
                File runDataFolder = new File(runDataFolderName);
                runDataFolder.mkdirs();

                String batteryStatsFilename = runDataFolderName + "batterystats";
                String systraceFilename = runDataFolderName + "systrace";
                String traceviewFilename = runDataFolderName + "tracedump";

                PETrA.executeCommand("adb shell pm clear " + appName, null, null, true);

                System.out.println("Resetting battery stats.");
                PETrA.executeCommand("adb shell dumpsys batterystats --reset", null, null, true);

                System.out.println("Opening app.");
                PETrA.executeCommand("adb shell input keyevent 82", null, null, true);
                PETrA.executeCommand("adb shell monkey -p " + appName + " 1", null, null, true);
                PETrA.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable true", null, null, true);

                System.out.println("Start profiling.");
                PETrA.executeCommand("adb shell am profile start " + appName + " ./data/local/tmp/log.trace", null, null, true);
                Date time1 = new Date();

                System.out.println("Capturing system traces.");
                SysTraceRunner sysTraceRunner = new SysTraceRunner(timeCapturing, systraceFilename, platformToolsFolder);
                Thread systraceThread = new Thread(sysTraceRunner);
                systraceThread.start();

                System.out.println("Executing random actions.");
                Random random = new Random();
                int seed = random.nextInt();
                PETrA.executeCommand("adb kill-server", null, null, true);
                PETrA.executeCommand("adb start-server", null, null, true);
                PETrA.executeCommand("adb shell monkey -p " + appName + " -s " + seed + " --throttle " + timeBetweenInteractions + " --ignore-crashes --ignore-timeouts --ignore-security-exceptions " + interactions, null, null, true);

                Date time2 = new Date();
                long timespent = time2.getTime() - time1.getTime();

                timeCapturing = (int) ((timespent + 10000) / 1000);

                System.out.println("Stop profiling.");
                PETrA.executeCommand("adb shell am profile stop " + appName, null, null, true);

                System.out.println("Saving battery stats.");
                PETrA.executeCommand("adb shell dumpsys batterystats", null, new File(batteryStatsFilename), true);

                System.out.println("Saving traceviews.");
                PETrA.executeCommand("adb pull ./data/local/tmp/log.trace " + runDataFolderName, null, null, true);
                PETrA.executeCommand("./dmtracedump -o " + runDataFolderName + "log.trace", platformToolsFolder, new File(traceviewFilename), true);

                systraceThread.join();

                System.out.println("Loading power profile.");
                PowerProfile powerProfile = null;
                try {
                    powerProfile = PowerProfileParser.parseFile(powerProfileName);
                } catch (IOException ex) {
                    Logger.getLogger(PETrA.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("Elaborating traceview info.");
                List<TraceLine> traceLines;
                try {
                    traceLines = TraceViewParser.parseFile(traceviewFilename, appName, runDataFolderName);
                    int traceviewLength = traceLines.get(0).setTimeLength();
                    int traceviewStart = traceLines.get(0).getTraceViewStartingTime();

                    System.out.println("Elaborating battery stats info.");
                    List<EnergyInfo> energyInfoArray = BatteryStatsParser.parseFile(batteryStatsFilename, traceviewStart, traceviewLength, appName);

                    System.out.println("Elaborating sys trace stats info...");
                    SysTrace cpuInfo = SysTraceParser.main(systraceFilename, traceviewStart, traceviewLength);

                    System.out.println("Aggregating results");
                    try (PrintWriter resultsWriter = new PrintWriter(runDataFolderName + "result.csv", "UTF-8")) {
                        resultsWriter.println("signature, joule, seconds");
                        energyInfoArray = PETrA.mergeEnergyInfo(energyInfoArray, cpuInfo, powerProfile);
                        for (TraceLine traceLine : traceLines) {
                            List<Double> result = PETrA.calculateConsumption(traceLine.getEntrance(), traceLine.getExit(), energyInfoArray, powerProfile);
                            resultsWriter.println(traceLine.getSignature() + "," + result.get(0) + "," + result.get(1));
                        }

                        resultsWriter.flush();
                    }
                } catch (IOException | ParseException | InterruptedException | IndexOutOfBoundsException | NumberFormatException ex) {
                    run--;
                    trials++;
                    if (trials == 10) {
                        break;
                    } else {
                        continue;
                    }
                } finally {
                    System.out.println("Stop app.");
                    PETrA.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable false", null, null, true);
                    PETrA.executeCommand("adb shell am force-stop " + appName, null, null, true);

                    PETrA.executeCommand("adb shell pm clear " + appName, null, null, true);
                }
                try {
                    if (seedsWriter != null) {
                        seedsWriter.append(seed + "\n");
                        seedsWriter.flush();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(PETrA.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                if (seedsWriter != null) {
                    seedsWriter.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(PETrA.class.getName()).log(Level.SEVERE, null, ex);
            }

            PETrA.executeCommand("adb shell dumpsys battery reset", null, null, true);

            System.out.println("Uninstalling app.");
            PETrA.executeCommand("adb shell pm uninstall " + appName, null, null, true);
        }

    }

    private static void executeCommand(String command, File directoryFolder, File outputFile, boolean waitfor) {
        try {
            List<String> listCommands = new ArrayList<>();

            String[] arrayExplodedCommands = command.split(" ");
            listCommands.addAll(Arrays.asList(arrayExplodedCommands));
            ProcessBuilder pb = new ProcessBuilder(listCommands);
            if (directoryFolder != null) {
                pb.directory(directoryFolder);
            }
            pb.inheritIO();
            if (outputFile != null) {
                pb.redirectOutput(outputFile);
            }
            Process commandProcess = pb.start();
            if (waitfor == true) {
                commandProcess.waitFor();
                Thread.sleep(3000);
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(PETrA.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<EnergyInfo> mergeEnergyInfo(List<EnergyInfo> energyInfoArray, SysTrace cpuInfo, PowerProfile powerProfile) throws IOException, ParseException {
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

    public static List calculateConsumption(int timeEnter, int timeExit, List<EnergyInfo> energyInfoArray, PowerProfile powerProfile) throws IOException, ParseException {

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

}
