package it.unisa.petra.BatteryStats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Antonio Prota
 * @author Dario Di Nucci
 */
public class BatteryStatsParser {

    public static ArrayList<EnergyInfo> parseFile(String fileName, int traceviewStart, int traceviewLength) throws IOException {
        ArrayList<EnergyInfo> energyInfoArray = new ArrayList<>();
        File file = new File(fileName);

        int infoCounter = 0;

        String line;
        try (BufferedReader readAll = new BufferedReader(new FileReader(file))) {
            Pattern energyRowPattern = Pattern.compile("\\s*(\\d|.\\d*m*\\d*s*\\d*ms*)\\s.\\d.\\s\\d{3}\\s(.*)");
            while ((line = readAll.readLine()) != null) {
                Matcher matcher1 = energyRowPattern.matcher(line);
                int variationTime;

                if (matcher1.find()) {
                    variationTime = toMillisec(matcher1.group(1));
                    int realTime = variationTime + traceviewStart;
                    if (realTime < traceviewStart + traceviewLength) {
                        EnergyInfo energyInfo = new EnergyInfo();
                        energyInfo.setTime(realTime);

                        if (!energyInfoArray.isEmpty()) {
                            energyInfo.setDevices(energyInfoArray.get(infoCounter).getDevices());
                            energyInfo.setVoltage(energyInfoArray.get(infoCounter).getVoltage());
                        }

                        String devices = matcher1.group(2).split(":")[0];
                        String[] devicesArray = devices.split(" ");
                        for (String device : devicesArray) {
                            if (device.contains("volt=")) {
                                energyInfo.setVoltage(Integer.parseInt(device.replaceFirst("volt=", "")));
                            }
                            if (device.contains("-")) {
                                String deviceDeactivated = device.replaceFirst("-", "");
                                energyInfo.removeDevice(deviceDeactivated);
                            }
                            if (device.contains("+")) {
                                if (!device.replaceFirst("\\+", "").startsWith("top")) {
                                    energyInfo.addDevice(device.replaceFirst("\\+", ""));
                                }
                            }
                        }
                        if (energyInfoArray.isEmpty()) {
                            energyInfoArray.add(energyInfo);
                        } else {
                            if (!energyInfo.equals(energyInfoArray.get(infoCounter))) {
                                energyInfoArray.add(energyInfo);
                            }
                        }
                    } else {
                        break;
                    }
                    infoCounter = energyInfoArray.size() - 1;
                }
            }
        }

        return energyInfoArray;
    }

    private static int toMillisec(String time) {
        int h = 0;
        int m = 0;
        int s = 0;
        int ms = 0;
        Pattern pattern1 = Pattern.compile("(\\d)h(\\d{2})m(\\d{2})s(\\d{3})ms");
        Pattern pattern2 = Pattern.compile("(\\d*)m(\\d{2})s(\\d{3})ms");
        Pattern pattern3 = Pattern.compile("(\\d*)s(\\d{3})ms");
        Pattern pattern4 = Pattern.compile("(\\d*)ms");
        Matcher matcher1 = pattern1.matcher(time);
        Matcher matcher2 = pattern2.matcher(time);
        Matcher matcher3 = pattern3.matcher(time);
        Matcher matcher4 = pattern4.matcher(time);
        if (matcher1.find()) {
            h = Integer.parseInt(matcher1.group(1));
            m = Integer.parseInt(matcher1.group(2));
            s = Integer.parseInt(matcher1.group(3));
            ms = Integer.parseInt(matcher1.group(4));
        } else if (matcher2.find()) {
            m = Integer.parseInt(matcher2.group(1));
            s = Integer.parseInt(matcher2.group(2));
            ms = Integer.parseInt(matcher2.group(3));
        } else if (matcher3.find()) {
            s = Integer.parseInt(matcher3.group(1));
            ms = Integer.parseInt(matcher3.group(2));
        } else if (matcher4.find()) {
            ms = Integer.parseInt(matcher4.group(1));
        }
        if (s != 0) {
            s = s * 1000;
        }
        if (m != 0) {
            m = m * 60000;
        }
        if (h != 0) {
            h = h * 3600000;
        }
        return h + m + s + ms;
    }

}
