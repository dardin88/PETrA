package it.unisa.petra.BatteryStats;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonio Prota
 */
public class EnergyInfo {

    private int time;
    private int volt;
    private List<String> devices;
    private float consumption;
    private int cpuFreq;

    public EnergyInfo() {
        this.devices = new ArrayList<>();
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getVoltage() {
        return volt;
    }

    public void setVoltage(int volt) {
        this.volt = volt;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public void addDevice(String device) {
        this.devices.add(device);
    }

    public void removeDevice(String device) {
        for (int i = 0; i < this.getDevices().size(); i++) {
            if (this.getDevices().get(i).equals(device)) {
                this.devices.remove(i);
            }
        }
    }

    public float getConsumption() {
        return consumption;
    }

    public void setConsumption(float consumption) {
        this.consumption = consumption;
    }

    public int getCpuFreq() {
        return cpuFreq;
    }

    public void setCpuFreq(int cpuFreq) {
        this.cpuFreq = cpuFreq;
    }
}
