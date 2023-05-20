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
class PerfettoRunner implements Runnable {

    private final int timeCapturing;
    private final String systraceFilename;

    PerfettoRunner(int timeCapturing, String systraceFilename) {
        this.timeCapturing = timeCapturing;
        this.systraceFilename = systraceFilename;
    }

    private static void executeCommand(String command) {
        try {
            List<String> listCommands = new ArrayList<>();

            String[] arrayExplodedCommands = command.split(" ");
            listCommands.addAll(Arrays.asList(arrayExplodedCommands));
            ProcessBuilder pb = new ProcessBuilder(listCommands);

            /* FIXME ignore output */
            pb.redirectOutput(ProcessBuilder.Redirect.to(new File("/dev/null")));
            pb.redirectError(ProcessBuilder.Redirect.to(new File("/dev/null")));

            java.lang.Process commandProcess = pb.start();
            commandProcess.waitFor();
            Thread.sleep(3000);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(PerfettoRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String command = "./tools/record_android_trace -n -o " + this.systraceFilename + " -t " + this.timeCapturing + "s freq idle";
        PerfettoRunner.executeCommand(command);
    }
}
