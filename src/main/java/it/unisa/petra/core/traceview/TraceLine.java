package it.unisa.petra.core.traceview;

/**
 *
 * @author Dario Di Nucci
 */
public class TraceLine {
    private int entrance;
    private int exit;
    private String signature;


    public int getEntrance() {
        return entrance;
    }

    void setEntrance(int entrance) {
        this.entrance = entrance;
    }

    public int getExit() {
        return exit;
    }

    void setExit(int exit) {
        this.exit = exit;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
    
}
