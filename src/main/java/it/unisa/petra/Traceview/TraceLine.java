package it.unisa.petra.Traceview;

/**
 *
 * @author Dario Di Nucci
 */
public class TraceLine {
    private int traceId;
    private int entrance;
    private int exit;
    private String signature;
    private TraceLine caller;
    private int timeLength;
    private int startTraceViewTime; //tempo di inizio in ms del monitor di traceview

    public int getTraceId() {
        return traceId;
    }

    public void setTraceId(int traceId) {
        this.traceId = traceId;
    }

    public int getEntrance() {
        return entrance;
    }

    public void setEntrance(int entrance) {
        this.entrance = entrance;
    }

    public int getExit() {
        return exit;
    }

    public void setExit(int exit) {
        this.exit = exit;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public TraceLine getCaller() {
        return caller;
    }

    public void setCaller(TraceLine caller) {
        this.caller = caller;
    }

    /**
     * @return the durata
     */
    public int setTimeLength() {
        return timeLength;
    }

    /**
     * @param timeLength the durata to set
     */
    public void setTimeLength(int timeLength) {
        this.timeLength = timeLength;
    }

    /**
     * @return the starttraceviewtime
     */
    public int getTraceViewStartingTime() {
        return startTraceViewTime;
    }

    /**
     * @param startTraceViewTime the starttraceviewtime to set
     */
    public void setStartTraceviewTime(int startTraceViewTime) {
        this.startTraceViewTime = startTraceViewTime;
    }
    
}
