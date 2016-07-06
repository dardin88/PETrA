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
    private int systraceFinishTime;

    public List<CpuFreq> getFrequency() {
        return freq;
    }

    public void setFrequency(List<CpuFreq> freq) {
        this.freq = freq;
    }

    public List<CpuIdle> getIdle() {
        return idle;
    }

    public void setIdle(List<CpuIdle> idle) {
        this.idle = idle;
    }

    public int getSystraceStartTime() {
        return systraceStartEntrance;
    }

    public void setSystraceStartTime(int startSystraceEntrance) {
        this.systraceStartEntrance = startSystraceEntrance;
    }

    public int getSystraceFinishTime() {
        return systraceFinishTime;
    }

    public void setSystraceFinishTime(int systraceFinishTime) {
        this.systraceFinishTime = systraceFinishTime;
    }

}
