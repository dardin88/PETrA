package it.unisa.petra.core.powerprofile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Antonio Prota
 * @author Dario Di Nucci
 */
public class PowerProfileParser {

    public static PowerProfile parseFile(String fileName) throws IOException {

        PowerProfile powerProfile = new PowerProfile();

        try {
            HashMap<String, Double> devices = new HashMap<>();
            List<CpuClusterInfo> cpuInfo = new ArrayList<>();
            List<Double> radioInfo = new ArrayList<>();

            Pattern cpuClustustersCore = Pattern.compile("cpu\\.clusters\\.cores");
            Pattern cpuConsumptionPattern = Pattern.compile("cpu\\.active(\\.cluster(\\d))?");
            Pattern cpuFrequencyPattern = Pattern.compile("cpu\\.speeds(\\.cluster(\\d))?");
            Pattern radioPattern = Pattern.compile("radio\\.on");

            File powerProfileFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(powerProfileFile);
            doc.getDocumentElement().normalize();

            NodeList deviceList = doc.getElementsByTagName("item");

            for (int i = 0; i < deviceList.getLength(); i++) {
                Element e = (Element) deviceList.item(i);
                devices.put(e.getAttribute("name"), Double.parseDouble(e.getTextContent()));
            }

            NodeList cpuRadioInfoList = doc.getElementsByTagName("array");
            for (int i = 0; i < cpuRadioInfoList.getLength(); i++) {
                Element e = (Element) cpuRadioInfoList.item(i);
                Matcher cpuClustersCoreMatcher = cpuClustustersCore.matcher(e.getAttribute("name"));
                if (cpuClustersCoreMatcher.find()) {
                    for (int j = 0; j < e.getElementsByTagName("value").getLength(); j++) {
                        cpuInfo.add(new CpuClusterInfo());
                        cpuInfo.get(j).setNumCores(Integer.parseInt(e.getElementsByTagName("value").item(j).getTextContent()));
                    }
                }

                Matcher cpuConsumptionMatcher = cpuConsumptionPattern.matcher(e.getAttribute("name"));
                if (cpuConsumptionMatcher.find()) {
                    int clusterNumber = 0;
                    if (cpuConsumptionMatcher.group(2) != null) {
                        clusterNumber = Integer.parseInt(cpuConsumptionMatcher.group(2));
                    }
                    if (cpuInfo.isEmpty()) {
                        cpuInfo.add(new CpuClusterInfo());
                    }
                    for (int j = 0; j < e.getElementsByTagName("value").getLength(); j++) {
                        cpuInfo.get(clusterNumber).addConsumption(Double.parseDouble(e.getElementsByTagName("value").item(j).getTextContent()));
                    }
                }

                Matcher cpuFrequencyMatcher = cpuFrequencyPattern.matcher(e.getAttribute("name"));
                if (cpuFrequencyMatcher.find()) {
                    int clusterNumber = 0;
                    if (cpuFrequencyMatcher.group(2) != null) {
                        clusterNumber = Integer.parseInt(cpuFrequencyMatcher.group(2));
                    }
                    if (cpuInfo.isEmpty()) {
                        cpuInfo.add(new CpuClusterInfo());
                    }
                    for (int j = 0; j < e.getElementsByTagName("value").getLength(); j++) {
                        cpuInfo.get(clusterNumber).addFrequency(Integer.parseInt(e.getElementsByTagName("value").item(j).getTextContent()));
                    }
                }

                Matcher radioMatcher = radioPattern.matcher(e.getAttribute("name"));
                if (radioMatcher.find()) {
                    for (int j = 0; j < e.getElementsByTagName("value").getLength(); j++) {
                        radioInfo.add(Double.parseDouble(e.getElementsByTagName("value").item(j).getTextContent()));
                    }
                }
            }

            for (CpuClusterInfo aCpuInfo : cpuInfo) {
                aCpuInfo.mergeFrequenciesAndConsumptions();
            }

            powerProfile.setDevices(devices);
            powerProfile.setCpuInfo(cpuInfo);
            powerProfile.setRadioInfo(radioInfo);
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(PowerProfileParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        return powerProfile;

    }

}
