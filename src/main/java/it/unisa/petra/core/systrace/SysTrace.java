package it.unisa.petra.core.systrace;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dardin88
 * @author Antonio Prota
 */
public class SysTrace {

    private List<CpuFrequency> frequencies = new ArrayList<>();
    private int systraceStartEntrance;
    private int numberOfCpu;

    public List<CpuFrequency> getFrequencies() {
        return frequencies;
    }

    void setFrequencies(List<CpuFrequency> freq) {
        this.frequencies = freq;
    }

    public int getSystraceStartTime() {
        return systraceStartEntrance;
    }

    void setSystraceStartTime(int startSystraceEntrance) {
        this.systraceStartEntrance = startSystraceEntrance;
    }

    public int getNumberOfCpu() {
        return numberOfCpu;
    }

    public void setNumberOfCpu(int numberOfCpu) {
        this.numberOfCpu = numberOfCpu;
    }
}
