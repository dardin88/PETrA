package it.unisa.petra.SysTrace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dario Di Nucci
 */
public class SysTraceRunner implements Runnable {

    private final int timeCapturing;
    private final String systraceFilename;
    private final File platformToolsFolder;

    public SysTraceRunner(int timeCapturing, String systraceFilename, File platformToolsFolder) {
        this.timeCapturing = timeCapturing;
        this.systraceFilename = systraceFilename;
        this.platformToolsFolder = platformToolsFolder;
    }

    @Override
    public void run() {
        SysTraceRunner.executeCommand("python systrace/systrace.py --time=" + this.timeCapturing / 1000 + " freq -o " + this.systraceFilename, this.platformToolsFolder, null, true);
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
            Logger.getLogger(SysTraceRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
