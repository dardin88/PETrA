package it.unisa.petra.process;

/**
 *
 * @author dardin88
 */
public class PETrAProcessOutput {

    private final int timeCapturing;
    private final String seed;

    PETrAProcessOutput(int timeCapturing, String seed) {
        this.timeCapturing = timeCapturing;
        this.seed = seed;
    }

    public int getTimeCapturing() {
        return timeCapturing;
    }

    public String getSeed() {
        return seed;
    }

}
