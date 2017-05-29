package it.unisa.petra.core;

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
            Logger.getLogger(SysTraceRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String command1 = "adb kill-server";
        String command2 = "adb start-server";
        String command3 = "python " + platformToolsFolder + "/systrace/systrace.py --time=" + this.timeCapturing + " freq idle -o " + this.systraceFilename;

        SysTraceRunner.executeCommand(command1);
        SysTraceRunner.executeCommand(command2);
        SysTraceRunner.executeCommand(command3);
    }
}
