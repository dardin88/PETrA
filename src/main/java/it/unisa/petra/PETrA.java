package it.unisa.petra;

import it.unisa.petra.process.NoDeviceFoundException;
import it.unisa.petra.process.PETrAProcess;
import it.unisa.petra.process.PETrAProcessOutput;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class PETrA {

    public static void main(String[] args) {
        try {
            String powerProfilePath = ConfigManager.getPowerProfileFile();
            String platformToolsFolderPath = ConfigManager.getPlatformToolsFolder();
            int runs = ConfigManager.getMaxRun();
            int interactions = ConfigManager.getInteractions();
            int timeBetweenInteractions = ConfigManager.getTimeBetweenInteractions();
            String appName = ConfigManager.getAppName();
            String outputLocationPath = ConfigManager.getOutputLocation();
            String apkLocationPath = ConfigManager.getApkLocation();

            PETrAProcess process = new PETrAProcess();

            File appDataFolder = new File(outputLocationPath);
            appDataFolder.mkdirs();
            File seedsFile = new File(outputLocationPath + "seeds");
            BufferedWriter seedsWriter = new BufferedWriter(new FileWriter(seedsFile, true));
            process.installApp(apkLocationPath);
            int trials = 0;
            int timeCapturing = (interactions * timeBetweenInteractions) / 1000;
            for (int run = 0; run < runs; run++) {
                PETrAProcessOutput output = process.playRun(run, appName, interactions, timeBetweenInteractions, timeCapturing,
                        "", platformToolsFolderPath, powerProfilePath, outputLocationPath);

                if (output == null) {
                    run--;
                    trials++;
                } else {
                    seedsWriter.append(output.getSeed() + "\n");
                    timeCapturing = output.getTimeCapturing();
                }
            }
            process.uninstallApp(appName);
        } catch (IOException | InterruptedException | NoDeviceFoundException ex) {
            Logger.getLogger(PETrA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
