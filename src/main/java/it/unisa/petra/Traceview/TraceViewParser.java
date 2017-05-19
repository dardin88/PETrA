package it.unisa.petra.Traceview;

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
public class TraceViewParser {

    public static TraceviewStructure parseFile(String fileName, String filter) throws IOException, NumberFormatException {

        File file = new File(fileName);

        String readLine;

        int firstRowTime = 0;
        int actualRowTime = 0;

        Pattern traceViewPattern = Pattern.compile("(\\d*)\\s(\\w{3})\\s*(\\d*)[\\s|-](.*)");
        Pattern processPattern = Pattern.compile("(\\d*)\\smain");

        ArrayList<TraceLine> tracelines = new ArrayList<>();
        try (BufferedReader readAll = new BufferedReader(new FileReader(file))) {
            boolean firstRow = true;
            int processId = 0;
            while ((readLine = readAll.readLine()) != null) {
                if (processId == 0) {
                    Matcher matcher1 = processPattern.matcher(readLine);
                    if (matcher1.find()) {
                        processId = Integer.parseInt(matcher1.group(1));
                    }
                }
                Matcher matcher2 = traceViewPattern.matcher(readLine);
                if (!readLine.contains("methodId")) {
                    if (matcher2.find()) {
                        int traceID = Integer.parseInt(matcher2.group(1));
                        String action = matcher2.group(2);
                        int usecs = Integer.parseInt(matcher2.group(3));
                        actualRowTime = (int) Math.round(usecs * 0.001); //convert the time from microseconds to milliseconds

                        if (firstRow) {
                            firstRowTime = actualRowTime;
                            firstRow = false;
                        }

                        String signature = matcher2.group(4);

                        boolean toFilter = false;
                        if (traceID == processId) {
                            toFilter = signature.contains(filter);
                        }

                        if (toFilter) {
                            if (action.equals("ent")) {
                                TraceLine tl = new TraceLine();
                                tl.setEntrance(actualRowTime);
                                tl.setSignature(signature);
                                tracelines.add(tl);
                            } else if (action.equals("xit")) {
                                for (int i = 0; i < tracelines.size(); i++) {
                                    TraceLine tl = tracelines.get(i);
                                    if (tl.getSignature().equals(signature) && tl.getExit() == 0) {
                                        tl.setExit(actualRowTime);
                                        tracelines.set(i, tl);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            readAll.close();
        }
        int timeLength = actualRowTime - firstRowTime;

        return new TraceviewStructure(tracelines, firstRowTime, timeLength);
    }

}
