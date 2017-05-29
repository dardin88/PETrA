package it.unisa.petra.core.systrace;

/**
 * @author Antonio Prota
 */
public class CpuFreq {
    private int cpuId;
    private int time;
    private int value;

    public int getCpuId() {
        return cpuId;
    }

    void setCpuId(int cpuId) {
        this.cpuId = cpuId;
    }

    /**
     * @return the time
     */
    public int getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    void setValue(int value) {
        this.value = value;
    }
}
