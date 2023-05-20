package it.unisa.petra.core.batterystats;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio Prota
 */
public class EnergyInfo {

    private final List<String> devices;
    private int entrance;
    private int exit;
    private int volt;
    private List<Integer> cpuFrequencies;
    private int phoneSignalStrength;

    EnergyInfo() {
        this.cpuFrequencies = new ArrayList<>();
        this.devices = new ArrayList<>();
    }

    public EnergyInfo(EnergyInfo toClone) {
        this.entrance = toClone.getEntrance();
        this.exit = toClone.getExit();
        this.volt = toClone.getVoltage();
        this.devices = new ArrayList<>(toClone.getDevices());
        this.cpuFrequencies = new ArrayList<>(toClone.getCpuFrequencies());
        this.phoneSignalStrength = toClone.getPhoneSignalStrength();
    }

    public int getEntrance() {
        return entrance;
    }

    public void setEntrance(int entrance) {
        this.entrance = entrance;
    }

    public int getExit() {
        return exit;
    }

    void setExit(int exit) {
        this.exit = exit;
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

    public List<Integer> getCpuFrequencies() {
        return cpuFrequencies;
    }

    public void setCpuFrequencies(List<Integer> cpuFrequencies) {
        this.cpuFrequencies = new ArrayList<>(cpuFrequencies);
    }

    public int getPhoneSignalStrength() {
        return phoneSignalStrength;
    }

    void setPhoneSignalStrength(int phoneSignalStrength) {
        this.phoneSignalStrength = phoneSignalStrength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnergyInfo that = (EnergyInfo) o;

        return entrance == that.entrance && exit == that.exit && volt == that.volt && cpuFrequencies.equals(that.cpuFrequencies) &&
                devices.equals(that.devices) && phoneSignalStrength == that.phoneSignalStrength;
    }
}
