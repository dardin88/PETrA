package it.unisa.petra.PowerProfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Antonio Prota
 * @author Dario Di Nucci
 */
public class PowerProfileParser {

    public static PowerProfile parseFile(String fileName) throws IOException {

        PowerProfile powerProfile = new PowerProfile();

        try {
            HashMap<String, Double> devices = new HashMap<>();
            ArrayList<Double> radioInfo = new ArrayList<>();
            ArrayList<Integer> speedValues = new ArrayList<>();
            ArrayList<Double> speedActives = new ArrayList<>();
            HashMap<Integer, Double> cpuInfo = new HashMap<>();

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
                switch (e.getAttribute("name")) {
                    case "radio.on":
                        for (int j = 0; j < e.getElementsByTagName("value").getLength(); j++) {
                            radioInfo.add(Double.parseDouble(e.getElementsByTagName("value").item(j).getTextContent()));
                        }   break;
                    case "cpu.speeds":
                        for (int j = 0; j < e.getElementsByTagName("value").getLength(); j++) {
                            speedValues.add(Integer.parseInt(e.getElementsByTagName("value").item(j).getTextContent()));
                        }   break;
                    case "cpu.active":
                        for (int j = 0; j < e.getElementsByTagName("value").getLength(); j++) {
                            speedActives.add(Double.parseDouble(e.getElementsByTagName("value").item(j).getTextContent()));
                        }   break;
                    default:
                        break;
                }
            }

            for (int i = 0; i < speedValues.size(); i++) {
                cpuInfo.put(speedValues.get(i), speedActives.get(i));
            }

            powerProfile.setDevices(devices);
            powerProfile.setCpuInfo(cpuInfo);
            powerProfile.setRadioInfo(radioInfo);
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(PowerProfileParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return powerProfile;

    }

    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }
}
