package it.unisa.petra.core;

/**
 *
 * @author dardin88
 */
public class ProcessOutput {

    private final int runTime;
    private final int seed;

    ProcessOutput(int runTime, int seed) {
        this.runTime = runTime;
        this.seed = seed;
    }

    public int getTimeCapturing() {
        return runTime;
    }

    public int getSeed() {
        return seed;
    }

}
