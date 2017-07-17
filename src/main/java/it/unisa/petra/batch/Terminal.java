package it.unisa.petra.batch;

import it.unisa.petra.core.Process;
import it.unisa.petra.core.ProcessOutput;
import it.unisa.petra.core.exceptions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dardin88
 */
public class Terminal {

    public static void run(String configFileLocation) {
        try {
            Process process = new Process();
            int trials = 0;
            BufferedWriter seedsWriter = null;

            ConfigManager configManager = new ConfigManager(configFileLocation);

            File appDataFolder = new File(configManager.getOutputLocation());

            appDataFolder.delete();
            appDataFolder.mkdirs();

            String appName = process.extractAppName(configManager.getApkLocationPath());

            if (configManager.getScriptLocationPath().isEmpty()) {
                File seedsFile = new File(configManager.getOutputLocation() + File.separator + "seeds");
                seedsWriter = new BufferedWriter(new FileWriter(seedsFile, true));
            }
            File apkFile = new File(configManager.getApkLocationPath());
            if (apkFile.exists()) {
                process.installApp(configManager.getApkLocationPath());
            } else {
                throw new ApkNotFoundException();
            }

            String powerProfilePath = configManager.getPowerProfileFile();

            if (powerProfilePath.isEmpty()) {
                process.extractPowerProfile(configManager.getOutputLocation());
                powerProfilePath = configManager.getOutputLocation() + "/power_profile.xml";
            }

            int timeCapturing = (configManager.getInteractions() * configManager.getTimeBetweenInteractions()) / 1000;

            if (timeCapturing <= 0) {
                timeCapturing = 100;
            }

            if (!configManager.getScriptLocationPath().isEmpty()) {
                timeCapturing = Integer.parseInt(configManager.getScriptTime());
            }

            for (int run = 1; run <= configManager.getRuns(); run++) {
                try {
                    if (trials == configManager.getTrials()) {
                        throw new NumberOfTrialsExceededException();
                    }
                    ProcessOutput output = process.playRun(run, appName, configManager.getInteractions(),
                            configManager.getTimeBetweenInteractions(), timeCapturing, configManager.getScriptLocationPath(),
                            powerProfilePath, configManager.getOutputLocation(), appName);
                    if (seedsWriter != null) {
                        seedsWriter.append(String.valueOf(output.getSeed())).append("\n");
                    }
                    timeCapturing = output.getTimeCapturing();
                } catch (InterruptedException | IOException ex) {
                    run--;
                    trials++;
                }
            }
            process.uninstallApp(appName);
        } catch (ApkNotFoundException | AppNameCannotBeExtractedException | NoDeviceFoundException | IOException |
                NumberOfTrialsExceededException | ADBNotFoundException ex) {
            Logger.getLogger(Terminal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
