package it.unisa.petra.Traceview;

import it.unisa.petra.ConfigManager;
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

    public static ArrayList<TraceLine> parseFile(String fileName, String appName, String appDataFolder) throws IOException, NumberFormatException {

        File file = new File(fileName);

        String readLine;

        int firstRowTime = 0;
        int actualRowTime = 0;
        
        boolean getCompleteTrace;

        try {
            getCompleteTrace = ConfigManager.getCompleteTrace();
        } catch (IOException ioe) {
            getCompleteTrace = false;
        }

        Pattern traceViewPattern = Pattern.compile("(\\d*)\\s(\\w{3})\\s*(\\d*)[\\s|-](.*)");
        Pattern processPattern = Pattern.compile("(\\d*)\\smain");

        ArrayList<TraceLine> completeList = new ArrayList<>();
        try (BufferedReader readAll = new BufferedReader(new FileReader(file))) {
            TraceLine caller = new TraceLine();
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

                        boolean filter = false;
                        if (traceID == processId) {
                            filter = true;
                        }
                        if (!getCompleteTrace) {
                            filter &= signature.contains(appName);
                        }

                        if (filter) {
                            if (action.equals("ent")) {
                                TraceLine tl = new TraceLine();
                                tl.setTraceId(traceID);
                                tl.setEntrance(actualRowTime);
                                tl.setSignature(signature);
                                tl.setCaller(caller);
                                completeList.add(tl);
                                caller = tl;
                            } else if (action.equals("xit")) {
                                for (int i = 0; i < completeList.size(); i++) {
                                    TraceLine tl = completeList.get(i);
                                    if (tl.getSignature().equals(signature)) {
                                        tl.setExit(actualRowTime);

                                        completeList.set(i, tl);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            readAll.close();
        }
        int lastRowTime = actualRowTime;
        int timeLength = lastRowTime - firstRowTime;

        completeList.get(0).setTimeLength(timeLength);
        completeList.get(0).setStartTraceviewTime(firstRowTime);
        return completeList;
    }

}
