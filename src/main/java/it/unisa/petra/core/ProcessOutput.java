package it.unisa.petra.core;

/**
 *
 * @author dardin88
 */
public class ProcessOutput {

    private final int runTime;
    private final String seed;

    ProcessOutput(int runTime, String seed) {
        this.runTime = runTime;
        this.seed = seed;
    }

    public int getTimeCapturing() {
        return runTime;
    }

    public String getSeed() {
        return seed;
    }

}
