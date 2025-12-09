package it.unisa.petra.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs Perfetto trace capture instead of Systrace.
 */
public class PerfettoRunner implements Runnable {

    private final int timeCapturing;
    private final String perfettoFilename;
    private final String platformToolsFolder;

    public PerfettoRunner(int timeCapturing, String perfettoFilename, String platformToolsFolder) {
        this.timeCapturing = timeCapturing;
        this.perfettoFilename = perfettoFilename;
        this.platformToolsFolder = platformToolsFolder;
    }

    private static void executeCommand(String command) {
        try {
            List<String> listCommands = new ArrayList<>();
            String[] arrayExplodedCommands = command.split(" ");
            listCommands.addAll(Arrays.asList(arrayExplodedCommands));
            ProcessBuilder pb = new ProcessBuilder(listCommands);
            pb.inheritIO();
            java.lang.Process commandProcess = pb.start();
            commandProcess.waitFor();
            Thread.sleep(3000);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(PerfettoRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        // Example Perfetto command, adjust as needed for your environment
        String command = platformToolsFolder + "/perfetto " +
                "-o " + this.perfettoFilename + " " +
                "-t " + this.timeCapturing + "s " +
                "-c " + platformToolsFolder + "/config.pfconf";
        PerfettoRunner.executeCommand(command);
    }
}
