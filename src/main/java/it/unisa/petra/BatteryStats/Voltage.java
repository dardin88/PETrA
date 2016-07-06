package it.unisa.petra.BatteryStats;

/**
 *
 * @author antonio-prota
 */
public class Voltage {

    private int time;
    private String volt;

    public Voltage(int time, String volt) {
        this.time = time;
        this.volt = volt;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getVolt() {
        return volt;
    }

    public void setVolt(String volt) {
        this.volt = volt;
    }
}
