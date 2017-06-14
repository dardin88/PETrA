package it.unisa.petra.core.systrace;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Antonio Prota
 * @author Dario Di Nucci
 */
public class SysTraceParser {

    private static ArrayList<CpuFrequency> frequencyList;

    public static SysTrace parseFile(String fileName, int traceviewStart, int traceviewLength) throws IOException {
        File file = new File(fileName);
        SysTrace systrace = new SysTrace();
        frequencyList = new ArrayList<>();
        boolean firstLine = true;
        int timeStart = 0;
        int timeFinish = 0;
        int timeread = 0;
        Pattern freqRowPattern = Pattern.compile(".* \\[\\d{3}].* (.*): cpu_frequency: state=(\\d*) cpu_id=(\\d)");
        Pattern idleRowPattern = Pattern.compile(".* \\[.*] .* (.*): cpu_idle: state=\\d* cpu_id=(\\d)");

        Document doc = Jsoup.parse(file, "UTF-8", fileName);
        Elements scriptElements = doc.getElementsByClass("trace-data");
        String sysTraceText = scriptElements.get(0).dataNodes().get(0).getWholeData();

        for (String line : sysTraceText.split("\n")) {

            CpuFrequency frequency = new CpuFrequency();

            Matcher freqMatcher = freqRowPattern.matcher(line);
            Matcher idleMatcher = idleRowPattern.matcher(line);

            boolean lineFound = false;

            if (freqMatcher.find()) {

                if (firstLine) {
                    String time = toMillisec(freqMatcher.group(1));
                    timeStart = Integer.parseInt(time) + traceviewStart;
                    timeFinish = Integer.parseInt(time) + traceviewLength;
                    firstLine = false;
                }

                timeread = Integer.parseInt(toMillisec(freqMatcher.group(1)));
                frequency.setTime(timeread);
                frequency.setValue(Integer.parseInt(freqMatcher.group(2)));
                int cpuId = Integer.parseInt(freqMatcher.group(3));
                frequency.setCpuId(cpuId);
                if (cpuId + 1 > systrace.getNumberOfCpu()) {
                    systrace.setNumberOfCpu(cpuId + 1);
                }
                lineFound = true;
            }

            if (idleMatcher.find()) {

                if (firstLine) {
                    String time = toMillisec(idleMatcher.group(1));
                    timeStart = Integer.parseInt(time) + traceviewStart;
                    timeFinish = Integer.parseInt(time) + traceviewLength;
                    firstLine = false;
                }

                timeread = Integer.parseInt(toMillisec(idleMatcher.group(1)));
                frequency.setTime(timeread);
                frequency.setValue(0);
                int cpuId = Integer.parseInt(idleMatcher.group(2));
                frequency.setCpuId(cpuId);
                if (cpuId + 1 > systrace.getNumberOfCpu()) {
                    systrace.setNumberOfCpu(cpuId + 1);
                }
                lineFound = true;
            }

            if (lineFound && timeread < timeFinish) {

                if (frequencyList.isEmpty()) {
                    frequencyList.add(frequency);
                } else {
                    int lastCoreFrequency = SysTraceParser.getLastCoreValue(frequency.getCore());
                    if (frequency.getValue() != lastCoreFrequency) {
                        frequencyList.add(frequency);
                    }
                }
            } else if (timeread > timeFinish) {
                break;
            }

        }
        systrace.setFrequencies(frequencyList);
        systrace.setSystraceStartTime(timeStart);
        return systrace;
    }

    private static String toMillisec(String time) {
        int s = 0;
        int dec = 0;
        int totaltime;
        Pattern pattern = Pattern.compile("(\\d*).(\\d{3}).*");
        Matcher matcher1 = pattern.matcher(time);
        if (matcher1.find()) {
            s = Integer.parseInt(matcher1.group(1));
            dec = Integer.parseInt(matcher1.group(2));
        }
        if (s != 0) {
            s = s * 1000;
        }
        totaltime = s + dec;
        return Integer.toString(totaltime);
    }

    private static int getLastCoreValue(int core) {
        List<CpuFrequency> shallowListCopy = (List<CpuFrequency>) frequencyList.clone();
        Collections.reverse(shallowListCopy);

        for (CpuFrequency frequency : shallowListCopy) {
            if (frequency.getCore() == core) {
                return frequency.getValue();
            }
        }
        return 0;
    }
}
