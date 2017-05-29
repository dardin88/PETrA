package it.unisa.petra.core.systrace;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Antonio Prota
 * @author Dario Di Nucci
 */
public class SysTraceParser {

    public static SysTrace parseFile(String fileName, int traceviewStart, int traceviewLength) throws IOException {
        File file = new File(fileName);
        SysTrace systrace = new SysTrace();
        List<CpuFreq> freqList = new ArrayList<>();
        List<CpuIdle> idleList = new ArrayList<>();
        int timeStart = 0;
        int timeFinish;
        Pattern freqRowPattern = Pattern.compile(".* \\[\\d{3}].* (.*): cpu_frequency: state=(\\d*) cpu_id=(\\d)");
        Pattern idleRowPattern = Pattern.compile(".* \\[.*] .* (.*): cpu_idle: state=(\\d*).*");

        Document doc = Jsoup.parse(file, "UTF-8", fileName);
        Elements scriptElements = doc.getElementsByClass("trace-data");
        String sysTraceText = scriptElements.get(0).dataNodes().get(0).getWholeData();

        for (String line : sysTraceText.split("\n")) {
            freqList = new ArrayList<>();
            idleList = new ArrayList<>();
            timeStart = 0;
            Matcher freqMatcher = freqRowPattern.matcher(line);
            if (freqMatcher.find()) {
                String time = toMillisec(freqMatcher.group(1));
                timeStart = Integer.parseInt(time);
                break;
            }
        }
        timeStart = timeStart + traceviewStart;
        timeFinish = timeStart + traceviewLength;

        for (String line : sysTraceText.split("\n")) {
            Matcher freqMatcher = freqRowPattern.matcher(line);
            Matcher idleMatcher = idleRowPattern.matcher(line);

            if (freqMatcher.find()) {
                CpuFreq freq = new CpuFreq();
                int timeread = Integer.parseInt(toMillisec(freqMatcher.group(1)));
                freq.setTime(timeread);
                freq.setValue(Integer.parseInt(freqMatcher.group(2)));
                freq.setCpuId(Integer.parseInt(freqMatcher.group(3)));
                if (timeread < timeFinish) {
                    freqList.add(freq);
                } else if (timeread > timeFinish) {
                    break;
                }
            }

            if (idleMatcher.find()) {
                CpuIdle idle = new CpuIdle();
                int timeread = Integer.parseInt(toMillisec(freqMatcher.group(1)));
                idle.setTime(timeread);
                idle.setValue(freqMatcher.group(2));
                if (timeread < timeFinish) {
                    if (timeread < timeStart) {
                        if (freqList.isEmpty()) {
                            idleList.add(idle);
                        } else {
                            idleList.set(0, idle);
                        }
                    } else {
                        idleList.add(idle);
                    }
                } else if (timeread > timeFinish) {
                    break;
                }
            }
        }
        systrace.setFrequency(freqList);
        systrace.setIdle(idleList);
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
}
