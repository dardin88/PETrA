package it.unisa.petra.process;

/**
 *
 * @author dardin88
 */
public class PETrAProcessOutput {

    int timeCapturing;
    String seed;

    public PETrAProcessOutput(int timeCapturing, String seed) {
        this.timeCapturing = timeCapturing;
        this.seed = seed;
    }

    public int getTimeCapturing() {
        return timeCapturing;
    }

    public void setTimeCapturing(int timeCapturing) {
        this.timeCapturing = timeCapturing;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

}
