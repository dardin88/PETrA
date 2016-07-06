package it.unisa.petra.PowerProfile;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Antonio Prota
 */
public class PowerProfile {

    private HashMap<String, Integer> devices = new HashMap<>();
    private HashMap<Integer,Integer> cpuInfo;
    private List<Integer> radioInfo;

    public HashMap<String, Integer>  getDevices() {
        return devices;
    }
    
    public void setDevices(HashMap<String, Integer> devices){
        this.devices = devices;
    }

    public HashMap<Integer, Integer> getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(HashMap<Integer, Integer> cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public List<Integer> getRadioInfo() {
        return radioInfo;
    }

    public void setRadioInfo(List<Integer> radioInfo) {
        this.radioInfo = radioInfo;
    }
}
