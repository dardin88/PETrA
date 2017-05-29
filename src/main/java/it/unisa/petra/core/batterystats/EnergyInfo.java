package it.unisa.petra.core.batterystats;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio Prota
 */
public class EnergyInfo {

    private int time;
    private int volt;
    private List devices;
    private List cpuFrequencies;

    EnergyInfo() {
        this.cpuFrequencies = new ArrayList();
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

    void setVoltage(int volt) {
        this.volt = volt;
    }

    public List<String> getDevices() {
        return devices;
    }

    void setDevices(List<String> devices) {
        this.devices = new ArrayList(devices);
    }

    void addDevice(String device) {
        this.devices.add(device);
    }

    void removeDevice(String device) {
        for (int i = 0; i < this.getDevices().size(); i++) {
            if (this.getDevices().get(i).equals(device)) {
                this.devices.remove(i);
            }
        }
    }

    public List getCpuFrequencies() {
        return cpuFrequencies;
    }

    public void setCpuFrequencies(List cpuFrequencies) {
        this.cpuFrequencies = cpuFrequencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnergyInfo that = (EnergyInfo) o;

        return time == that.time && volt == that.volt && cpuFrequencies.equals(that.cpuFrequencies) && devices.equals(that.devices);
    }
}
