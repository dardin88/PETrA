package it.unisa.petra.SysTrace;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Prota
 */
public class SysTrace {

    private List<CpuFreq> freq = new ArrayList<>();
    private List<CpuIdle> idle = new ArrayList<>();
    private int systraceStartEntrance;

    public List<CpuFreq> getFrequency() {
        return freq;
    }

    void setFrequency(List<CpuFreq> freq) {
        this.freq = freq;
    }

    public List<CpuIdle> getIdle() {
        return idle;
    }

    void setIdle(List<CpuIdle> idle) {
        this.idle = idle;
    }

    public int getSystraceStartTime() {
        return systraceStartEntrance;
    }

    void setSystraceStartTime(int startSystraceEntrance) {
        this.systraceStartEntrance = startSystraceEntrance;
    }

}
