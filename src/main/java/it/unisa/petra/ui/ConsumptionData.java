package it.unisa.petra.ui;

/**
 *
 * @author dardin88
 */
public class ConsumptionData {

    private String signature;
    private double joule;
    private double seconds;
    private int numOfTraces;

    public ConsumptionData(String signature, double joule, double seconds) {
        this.signature = signature;
        this.joule = joule;
        this.seconds = seconds;
        this.numOfTraces = 1;
    }

    public void setJoule(double joule) {
        this.joule = joule;
    }

    public void setSeconds(double seconds) {
        this.seconds = seconds;
    }

    public void setNumOfTraces(int numOfTraces) {
        this.numOfTraces = numOfTraces;
    }

    public String getSignature() {
        return signature;
    }

    public double getJoule() {
        return joule;
    }

    public double getSeconds() {
        return seconds;
    }

    public int getNumOfTraces() {
        return numOfTraces;
    }

}
