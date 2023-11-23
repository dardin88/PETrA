package it.unisa.petra.batch;

import it.unisa.petra.core.Process;
import it.unisa.petra.core.ProcessOutput;
import it.unisa.petra.core.exceptions.ADBNotFoundException;
import it.unisa.petra.core.exceptions.ApkNotFoundException;
import it.unisa.petra.core.exceptions.NoDeviceFoundException;
import it.unisa.petra.core.exceptions.NumberOfTrialsExceededException;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

class SANERExperiment {

    public static void main(String[] args) throws IOException {

        try {
            Process process = new Process();

            ArrayList<String> appNames = new ArrayList<>();
            ArrayList<String> apkNames = new ArrayList<>();

            String apksLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/PETrA_evaluation/prop_debug_apk/";

            String line;
            BufferedReader br = new BufferedReader(new FileReader(
                    "/home/dardin88/Desktop/energy_consumption_bad_smell/PETrA_evaluation/app_list.csv"));
            while ((line = br.readLine()) != null) {
                appNames.add(line);
                apkNames.add(line + ".apk");
            }
            br.close();

            for (int appCounter = 0; appCounter < appNames.size(); appCounter++) {
                int trials = 0;
                String appName = appNames.get(appCounter);
                String testLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/PETrA_evaluation/test-scripts/"
                        + appName + ".txt";
                String outputLocation = "/home/dardin88/Desktop/energy_consumption_bad_smell/PETrA_evaluation/test-data/"
                        + appName + "/";
                int maxRun = 0;
                int maxTrials = 0;
                int interactions = 0;
                int timeBetweenInteractions = 0;
                String powerProfilePath = "";

                try {
                    ConfigManager configManager = new ConfigManager("config.properties");
                    maxRun = configManager.getRuns();
                    maxTrials = configManager.getTrials();
                    interactions = configManager.getInteractions();
                    timeBetweenInteractions = configManager.getTimeBetweenInteractions();
                    powerProfilePath = configManager.getOutputLocation() + "/power_profile.xml";
                } catch (IOException ex) {
                    Logger.getLogger(EMSEExperiment.class.getName()).log(Level.SEVERE, null, ex);
                }

                File appDataFolder = new File(outputLocation);

                int timeCapturing = (interactions * timeBetweenInteractions) / 1000;

                appDataFolder.mkdirs();

                File seedsFile = new File(outputLocation + "seeds");
                BufferedWriter seedsWriter = new BufferedWriter(new FileWriter(seedsFile, true));

                File apkLocation = new File(apksLocation + apkNames.get(appCounter));
                if (apkLocation.exists()) {
                    process.installApp(apkNames.get(appCounter));
                } else {
                    seedsWriter.close();
                    throw new ApkNotFoundException();
                }

                for (int run = 1; run <= maxRun; run++) {
                    try {
                        if (trials == maxTrials) {
                            throw new NumberOfTrialsExceededException();
                        }
                        ProcessOutput output = process.playRun(run, appName, interactions,
                                timeBetweenInteractions, timeCapturing, testLocation,
                                powerProfilePath, outputLocation, "");
                        seedsWriter.append(String.valueOf(output.getSeed())).append("\n");
                        timeCapturing = output.getTimeCapturing();
                    } catch (InterruptedException | IOException ex) {
                        run--;
                        trials++;
                    }
                }
                process.uninstallApp(appName);
                seedsWriter.close();
            }
        } catch (ApkNotFoundException | FileNotFoundException | ADBNotFoundException | NoDeviceFoundException
                | NumberOfTrialsExceededException e) {
            e.printStackTrace();
        }

    }
}
