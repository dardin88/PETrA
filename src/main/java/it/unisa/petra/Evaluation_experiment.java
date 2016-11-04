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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Evaluation_experiment {

    public static void main(String[] args) throws InterruptedException, ParseException {

        ArrayList<String> appNames = new ArrayList<>();
        ArrayList<String> apkNames = new ArrayList<>();

        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/home/dardin88/Desktop/energy_consumption_bad_smell/PETrA_evaluation/prop_list.csv"));
            while ((line = br.readLine()) != null) {
                appNames.add(line);
                apkNames.add(line + ".apk");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Evaluation_experiment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Evaluation_experiment.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int appCounter = 0; appCounter < appNames.size(); appCounter++) {

            boolean toRepeat = true;

            while (toRepeat) {

                String appName = appNames.get(appCounter);
                String apkLocation = apkNames.get(appCounter);
                String outputLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/PETrA_evaluation/prop_test_data/" + appName + "/";
                String testLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/PETrA_evaluation/test-scripts/" + appName + ".txt";
                String powerProfileName = null;
                File platformToolsFolder = null;
                String toolsFolder = null;

                try {
                    powerProfileName = ConfigManager.getPowerProfileFile();
                    platformToolsFolder = new File(ConfigManager.getPlatformToolsFolder());
                    toolsFolder = ConfigManager.getToolsFolder();
                } catch (IOException ex) {
                    Logger.getLogger(Evaluation_experiment.class.getName()).log(Level.SEVERE, null, ex);
                }

                File appDataFolder = new File(outputLocation);

                int timeCapturing = 120;

                appDataFolder.mkdirs();

                Evaluation_experiment.executeCommand("adb kill-server", null, null, true);
                Evaluation_experiment.executeCommand("adb start-server", null, null, true);
                
                Evaluation_experiment.executeCommand("adb shell dumpsys battery set usb 0", null, null, true);

                System.out.println("Installing app.");
                Evaluation_experiment.executeCommand("adb install " + apkLocation, null, null, true);

                String runDataFolderName = outputLocation;
                File runDataFolder = new File(runDataFolderName);
                runDataFolder.mkdirs();

                String batteryStatsFilename = runDataFolderName + "batterystats";
                String systraceFilename = runDataFolderName + "systrace";
                String traceviewFilename = runDataFolderName + "tracedump";

                Evaluation_experiment.executeCommand("adb shell pm clear " + appName, null, null, true);

                System.out.println("Resetting battery stats.");
                Evaluation_experiment.executeCommand("adb shell dumpsys batterystats --reset", null, null, true);

                System.out.println("Opening app.");
                Evaluation_experiment.executeCommand("adb shell input keyevent 82", null, null, true);
                Evaluation_experiment.executeCommand("adb shell monkey -p " + appName + " 1", null, null, true);

                System.out.println("Start profiling.");
                Evaluation_experiment.executeCommand("adb shell am profile start " + appName + " ./data/local/tmp/log.trace", null, null, true);

                System.out.println("Capturing system traces.");
                SysTraceRunner sysTraceRunner = new SysTraceRunner(timeCapturing, systraceFilename, platformToolsFolder);
                Thread systraceThread = new Thread(sysTraceRunner);
                systraceThread.start();

                System.out.println("Executing test.");
                Evaluation_experiment.executeCommand(toolsFolder + "/monkeyrunner " + toolsFolder + "monkey_playback.py " + testLocation, null, null, true);

                System.out.println("Stop profiling.");
                Evaluation_experiment.executeCommand("adb shell am profile stop " + appName, null, null, true);

                System.out.println("Saving battery stats.");
                Evaluation_experiment.executeCommand("adb shell dumpsys batterystats", null, new File(batteryStatsFilename), true);
                
                System.out.println("Saving traceviews.");
                Evaluation_experiment.executeCommand("adb pull ./data/local/tmp/log.trace " + runDataFolderName, null, null, true);
                Evaluation_experiment.executeCommand("./dmtracedump -o " + runDataFolderName + "log.trace", platformToolsFolder, new File(traceviewFilename), true);

                systraceThread.join();

                System.out.println("Loading power profile.");
                PowerProfile powerProfile = null;
                try {
                    powerProfile = PowerProfileParser.parseFile(powerProfileName);
                } catch (IOException ex) {
                    Logger.getLogger(Evaluation_experiment.class.getName()).log(Level.SEVERE, null, ex);
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
                    PrintWriter resultsWriter = new PrintWriter(runDataFolderName + "result.csv", "UTF-8");
                    resultsWriter.println("signature, joule, seconds");
                    energyInfoArray = Evaluation_experiment.mergeEnergyInfo(energyInfoArray, cpuInfo, powerProfile);
                    for (TraceLine traceLine : traceLines) {
                        List<Double> result = Evaluation_experiment.calculateConsumption(traceLine.getEntrance(), traceLine.getExit(), energyInfoArray, powerProfile);
                        resultsWriter.println(traceLine.getSignature() + "," + result.get(0) + "," + result.get(1));
                    }

                    System.out.println("Stop app.");
                    Evaluation_experiment.executeCommand("adb shell am force-stop " + appName, null, null, true);

                    Evaluation_experiment.executeCommand("adb shell pm clear " + appName, null, null, true);

                    resultsWriter.flush();
                } catch (IOException | ParseException | InterruptedException | IndexOutOfBoundsException ex) {
                    continue;
                }
                
                toRepeat = false;

                Evaluation_experiment.executeCommand("adb shell dumpsys battery reset", null, null, true);

                System.out.println("Uninstalling app.");
                Evaluation_experiment.executeCommand("adb shell pm uninstall " + appName, null, null, true);
            }
            return;
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
            Logger.getLogger(Evaluation_experiment.class.getName()).log(Level.SEVERE, null, ex);
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
