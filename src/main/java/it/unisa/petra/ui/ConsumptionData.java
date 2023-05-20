package it.unisa.petra.ui;

/**
 * @author dardin88
 */
class ConsumptionData {

    private final String signature;
    private double joule;
    private double seconds;
    private int numOfTraces;

    ConsumptionData(String signature, double joule, double seconds) {
        this.signature = signature;
        this.joule = joule;
        this.seconds = seconds;
        this.numOfTraces = 1;
    }

    String getSignature() {
        return signature;
    }

    double getJoule() {
        return joule;
    }

    void setJoule(double joule) {
        this.joule = joule;
    }

    double getSeconds() {
        return seconds;
    }

    void setSeconds(double seconds) {
        this.seconds = seconds;
    }

    int getNumOfTraces() {
        return numOfTraces;
    }

    void setNumOfTraces(int numOfTraces) {
        this.numOfTraces = numOfTraces;
    }

}
