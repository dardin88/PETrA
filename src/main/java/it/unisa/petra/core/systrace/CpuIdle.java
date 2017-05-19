package it.unisa.petra.core.systrace;

/**
 * @author Antonio Prota
 */
class CpuIdle {
    private int time;
    private String value;

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
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    void setValue(String value) {
        this.value = value;
    }
}
