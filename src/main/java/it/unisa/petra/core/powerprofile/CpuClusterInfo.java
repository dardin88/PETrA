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
        this.numCores = 1;
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
        return this.info.get(frequency);
    }

    void addConsumption(Double consumption) {
        this.consumptions.add(consumption);
    }

    void mergeFrequenciesAndConsumptions() {
        for (int i = 0; i < this.frequencies.size(); i++) {
            this.info.put(this.frequencies.get(i), this.consumptions.get(i));
        }
    }
}
