package it.unisa.petra.experiments;

import it.unisa.petra.BatteryStats.BatteryStatsParser;
import it.unisa.petra.BatteryStats.EnergyInfo;
import it.unisa.petra.ConfigManager;
import it.unisa.petra.PowerProfile.PowerProfile;
import it.unisa.petra.PowerProfile.PowerProfileParser;
import it.unisa.petra.SysTrace.CpuFreq;
import it.unisa.petra.SysTrace.SysTrace;
import it.unisa.petra.SysTrace.SysTraceParser;
import it.unisa.petra.Traceview.TraceLine;
import it.unisa.petra.Traceview.TraceViewParser;
import it.unisa.petra.Traceview.TraceviewStructure;
import it.unisa.petra.process.SysTraceRunner;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EMSEExperiment {

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
        } catch (IOException ex) {
            Logger.getLogger(EMSEExperiment.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int appCounter = 0; appCounter < appNames.size(); appCounter++) {

            String appName = appNames.get(appCounter);
            String apkName = apkNames.get(appCounter);
            String outputLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/icse_experiment/member_ignoring_method_test_data/" + appName + "/";
            String apkLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/icse_experiment/member_ignoring_method_apks/" + apkName;
            String oldSeedsLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/icse_experiment/test_data/" + appName + "/seeds";
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
                Logger.getLogger(EMSEExperiment.class.getName()).log(Level.SEVERE, null, ex);
            }

            File appDataFolder = new File(outputLocation);

            int timeCapturing = (interactions * timeBetweenInteractions) / 1000;

            appDataFolder.mkdirs();

            File seedsFile = new File(outputLocation + "seeds");
            BufferedWriter seedsWriter = null;
            try {
                seedsWriter = new BufferedWriter(new FileWriter(seedsFile, true));
            } catch (IOException ex) {
                Logger.getLogger(EMSEExperiment.class.getName()).log(Level.SEVERE, null, ex);
            }

            ArrayList<String> seeds = new ArrayList<>();

            if (!oldSeedsLocation.isEmpty()) {
                File oldSeedsFile = new File(oldSeedsLocation);
                try {
                    seeds.addAll(Files.readAllLines(oldSeedsFile.toPath()));
                } catch (IOException ex) {
                    Logger.getLogger(EMSEExperiment.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            EMSEExperiment.executeCommand("adb shell dumpsys battery set usb 0", null, null);

            System.out.println("Installing app.");
            EMSEExperiment.executeCommand("adb install " + apkLocation, null, null);

            int trials = 0;

            for (int run = 0; run < maxRun; run++) {
                System.out.println("==========================RUN_" + run + "======================================");

                Random random = new Random();

                String seed;

                if (!seeds.isEmpty()) {
                    seed = seeds.get(run);
                } else {
                    seed = random.nextInt() + "";
                }

                System.out.println("Seed: " + seed);

                String runDataFolderName = outputLocation + "run_" + run + "/";
                File runDataFolder = new File(runDataFolderName);
                runDataFolder.mkdirs();

                String batteryStatsFilename = runDataFolderName + "batterystats";
                String systraceFilename = runDataFolderName + "systrace";
                String traceviewFilename = runDataFolderName + "tracedump";

                EMSEExperiment.executeCommand("adb shell pm clear " + appName, null, null);

                System.out.println("Resetting battery stats.");
                EMSEExperiment.executeCommand("adb shell dumpsys batterystats --reset", null, null);

                System.out.println("Opening app.");
                EMSEExperiment.executeCommand("adb shell input keyevent 82", null, null);
                EMSEExperiment.executeCommand("adb shell monkey -p " + appName + " 1", null, null);
                EMSEExperiment.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable true", null, null);

                System.out.println("Start profiling.");
                EMSEExperiment.executeCommand("adb shell am profile start " + appName + " ./data/local/tmp/log.trace", null, null);
                Date time1 = new Date();

                System.out.println("Capturing system traces.");
                SysTraceRunner sysTraceRunner = new SysTraceRunner(timeCapturing, systraceFilename, platformToolsFolder);
                Thread systraceThread = new Thread(sysTraceRunner);
                systraceThread.start();

                System.out.println("Executing random actions.");

                EMSEExperiment.executeCommand("adb kill-server", null, null);
                EMSEExperiment.executeCommand("adb start-server", null, null);
                EMSEExperiment.executeCommand("adb shell monkey -p " + appName + " -s " + seed + " --throttle " + timeBetweenInteractions + " --ignore-crashes --ignore-timeouts --ignore-security-exceptions " + interactions, null, null);

                Date time2 = new Date();
                long timespent = time2.getTime() - time1.getTime();

                timeCapturing = (int) ((timespent + 10000) / 1000);

                System.out.println("Stop profiling.");
                EMSEExperiment.executeCommand("adb shell am profile stop " + appName, null, null);

                System.out.println("Saving battery stats.");
                EMSEExperiment.executeCommand("adb shell dumpsys batterystats", null, new File(batteryStatsFilename));

                System.out.println("Saving traceviews.");
                EMSEExperiment.executeCommand("adb pull ./data/local/tmp/log.trace " + runDataFolderName, null, null);
                EMSEExperiment.executeCommand("./dmtracedump -o " + runDataFolderName + "log.trace", platformToolsFolder, new File(traceviewFilename));

                systraceThread.join();

                System.out.println("Loading power profile.");
                PowerProfile powerProfile = null;
                try {
                    powerProfile = PowerProfileParser.parseFile(powerProfileName);
                } catch (IOException ex) {
                    Logger.getLogger(EMSEExperiment.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("Elaborating traceview info.");
                try {
                    TraceviewStructure traceviewStructure = TraceViewParser.parseFile(traceviewFilename, appName);
                    List<TraceLine> traceLines = traceviewStructure.getTraceLines();
                    int traceviewLength = traceviewStructure.getEndTime();
                    int traceviewStart = traceviewStructure.getStartTime();

                    System.out.println("Elaborating battery stats info.");
                    List<EnergyInfo> energyInfoArray = BatteryStatsParser.parseFile(batteryStatsFilename, traceviewStart, traceviewLength);

                    System.out.println("Elaborating sys trace stats info...");
                    SysTrace cpuInfo = SysTraceParser.parseFile(systraceFilename, traceviewStart, traceviewLength);

                    System.out.println("Aggregating results");
                    try (PrintWriter resultsWriter = new PrintWriter(runDataFolderName + "result.csv", "UTF-8")) {
                        resultsWriter.println("signature, joule, seconds");
                        energyInfoArray = EMSEExperiment.mergeEnergyInfo(energyInfoArray, cpuInfo);
                        for (TraceLine traceLine : traceLines) {
                            List<Double> result = EMSEExperiment.calculateConsumption(traceLine.getEntrance(), traceLine.getExit(), energyInfoArray, powerProfile);
                            resultsWriter.println(traceLine.getSignature() + "," + result.get(0) + "," + result.get(1));
                        }

                        resultsWriter.flush();
                    }
                } catch (IOException | IndexOutOfBoundsException | NumberFormatException ex) {
                    run--;
                    trials++;
                    if (trials == 10) {
                        break;
                    } else {
                        continue;
                    }
                } finally {
                    System.out.println("Stop app.");
                    EMSEExperiment.executeCommand("adb shell am broadcast -a org.thisisafactory.simiasque.SET_OVERLAY --ez enable false", null, null);
                    EMSEExperiment.executeCommand("adb shell am force-stop " + appName, null, null);

                    EMSEExperiment.executeCommand("adb shell pm clear " + appName, null, null);
                }
                try {
                    if (seedsWriter != null) {
                        seedsWriter.append(seed).append("\n");
                        seedsWriter.flush();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(EMSEExperiment.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                if (seedsWriter != null) {
                    seedsWriter.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(EMSEExperiment.class.getName()).log(Level.SEVERE, null, ex);
            }

            EMSEExperiment.executeCommand("adb shell dumpsys battery reset", null, null);

            System.out.println("Uninstalling app.");
            EMSEExperiment.executeCommand("adb shell pm uninstall " + appName, null, null);
        }

    }

    private static void executeCommand(String command, File directoryFolder, File outputFile) {
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
            commandProcess.waitFor();
            Thread.sleep(3000);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(EMSEExperiment.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static List<EnergyInfo> mergeEnergyInfo(List<EnergyInfo> energyInfoArray, SysTrace cpuInfo) {
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

    private static List calculateConsumption(int timeEnter, int timeExit, List<EnergyInfo> energyInfoArray, PowerProfile powerProfile) {

        double joul = 0;
        double totalSeconds = 0;

        for (EnergyInfo anEnergyInfoArray : energyInfoArray) {
            int cpuFrequency = anEnergyInfoArray.getCpuFreq();
            double ampere = powerProfile.getCpuInfo().get(cpuFrequency) / 1000;
            for (String deviceString : anEnergyInfoArray.getDevices()) {
                if (deviceString.contains("wifi")) {
                    ampere += powerProfile.getDevices().get("wifi.on") / 1000;
                } else if (deviceString.contains("screen")) {
                    ampere += powerProfile.getDevices().get("screen.on") / 1000;
                } else if (deviceString.contains("gps")) {
                    ampere += powerProfile.getDevices().get("gps.on") / 1000;
                }
            }
            double watt = ampere * anEnergyInfoArray.getVoltage() / 1000;
            double microseconds = 0;
            if (timeEnter >= anEnergyInfoArray.getTime()) {
                if (timeEnter > anEnergyInfoArray.getTime()) {
                    microseconds = timeExit - anEnergyInfoArray.getTime();
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
