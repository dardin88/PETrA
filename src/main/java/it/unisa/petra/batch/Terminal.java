package it.unisa.petra.batch;

import it.unisa.petra.core.Process;
import it.unisa.petra.core.ProcessOutput;
import it.unisa.petra.core.exceptions.ADBNotFoundException;
import it.unisa.petra.core.exceptions.ApkNotFoundException;
import it.unisa.petra.core.exceptions.NoDeviceFoundException;
import it.unisa.petra.core.exceptions.NumberOfTrialsExceededException;

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

            String sdkLocationPath = System.getenv("ANDROID_HOME");

            ConfigManager configManager = new ConfigManager(configFileLocation);

            File appDataFolder = new File(configManager.getOutputLocation());

            appDataFolder.delete();
            appDataFolder.mkdirs();

            if (configManager.getScriptLocationPath().isEmpty()) {
                File seedsFile = new File(configManager.getOutputLocation() + File.separator + "seeds");
                seedsWriter = new BufferedWriter(new FileWriter(seedsFile, true));
            }
            File apkFile = new File(configManager.getApkLocationPath());
            if (apkFile.exists()) {
                process.installApp(configManager.getApkLocationPath(), sdkLocationPath);
            } else {
                throw new ApkNotFoundException();
            }
            int timeCapturing = (configManager.getInteractions() * configManager.getTimeBetweenInteractions()) / 1000;

            if (timeCapturing <= 0) {
                timeCapturing = 1;
            }

            Thread.sleep(5000);

            for (int run = 1; run <= configManager.getRuns(); run++) {
                try {
                    if (trials == configManager.getTrials()) {
                        throw new NumberOfTrialsExceededException();
                    }
                    ProcessOutput output = process.playRun(run, configManager.getAppName(), configManager.getInteractions(),
                            configManager.getTimeBetweenInteractions(), timeCapturing, configManager.getScriptLocationPath(),
                            sdkLocationPath, configManager.getPowerProfileFile(), configManager.getOutputLocation());
                    if (seedsWriter != null) {
                        seedsWriter.append(String.valueOf(output.getSeed())).append("\n");
                    }
                    timeCapturing = output.getTimeCapturing();
                } catch (InterruptedException | IOException ex) {
                    run--;
                    trials++;
                }
            }
            process.uninstallApp(configManager.getAppName(), sdkLocationPath);
        } catch (ApkNotFoundException | NoDeviceFoundException | IOException | InterruptedException | NumberOfTrialsExceededException | ADBNotFoundException ex) {
            Logger.getLogger(Terminal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
