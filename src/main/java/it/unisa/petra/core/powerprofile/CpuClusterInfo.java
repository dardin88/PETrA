package it.unisa.petra.core.powerprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author dardin88
 */
class CpuClusterInfo {
    private final List<Integer> frequencies;
    private final List<Double> consumptions;
    private final HashMap<Integer, Double> info;
    private int numCores;

    CpuClusterInfo() {
        frequencies = new ArrayList<>();
        consumptions = new ArrayList<>();
        info = new HashMap<>();
    }

    int getNumCores() {
        return numCores;
    }

    void setNumCores(int numCore) {
        this.numCores = numCore;
    }

    void addFrequency(Integer frequency) {
        this.frequencies.add(frequency);
    }

    double getConsumption(int frequency) {
        double minDiff = 0;
        int closestFrequency = 0;
        for (int key : this.info.keySet()) {
            int diff = Math.abs(key - frequency);
            if (diff < minDiff) {
                minDiff = diff;
                closestFrequency = key;
            }
        }
        return this.info.get(closestFrequency);
    }

    void addConsumption(Double consumption) {
        this.consumptions.add(consumption);
    }

    void setIdleConsumption(double idleConsumption) {
        this.info.put(0, idleConsumption);
    }

    void mergeFrequenciesAndConsumptions() {
        for (int i = 0; i < this.frequencies.size(); i++) {
            this.info.put(this.frequencies.get(i), this.consumptions.get(i));
        }
    }
}
