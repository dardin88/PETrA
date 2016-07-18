package it.unisa.petra.PowerProfile;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Antonio Prota
 */
public class PowerProfile {

    private HashMap<String, Double> devices = new HashMap<>();
    private HashMap<Integer, Double> cpuInfo;
    private List<Double> radioInfo;

    public HashMap<String, Double> getDevices() {
        return devices;
    }

    public void setDevices(HashMap<String, Double> devices) {
        this.devices = devices;
    }

    public HashMap<Integer, Double> getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(HashMap<Integer, Double> cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public List<Double> getRadioInfo() {
        return radioInfo;
    }

    public void setRadioInfo(List<Double> radioInfo) {
        this.radioInfo = radioInfo;
    }
}
