package it.unisa.petra.core.powerprofile;

import java.util.HashMap;
import java.util.List;

/**
 * @author dardin88
 * @author Antonio Prota
 */
public class PowerProfile {

    private HashMap<String, Double> devices = new HashMap<>();
    private List<CpuClusterInfo> cpuInfo;
    private List<Double> radioInfo;

    public HashMap<String, Double> getDevices() {
        return devices;
    }

    void setDevices(HashMap<String, Double> devices) {
        this.devices = devices;
    }

    public double getCpuConsumptionByFrequency(int cluster, int frequency) {
        return cpuInfo.get(cluster).getConsumption(frequency);
    }

    int getClusterByCore(int core) {

        int lowerLimit = 0;
        int upperLimit = cpuInfo.get(0).getNumCores();

        for (int i = 0; i < cpuInfo.size(); i++) {
            if (core >= lowerLimit && core < upperLimit) {
                return i;
            } else {
                lowerLimit += cpuInfo.get(i).getNumCores();
                upperLimit += cpuInfo.get(i + 1).getNumCores();
            }
        }

        return -1;

    }

    void setCpuInfo(List<CpuClusterInfo> cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public List<Double> getRadioInfo() {
        return radioInfo;
    }

    void setRadioInfo(List<Double> radioInfo) {
        this.radioInfo = radioInfo;
    }

    public int computeNumberOfCores() {
        int numberOfCores = 0;

        for (CpuClusterInfo cpuClusterInfo : cpuInfo) {
            numberOfCores += cpuClusterInfo.getNumCores();
        }

        return numberOfCores;
    }
}
