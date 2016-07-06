package it.unisa.petra;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Dario
 */
public class ConfigManager {

    private static InputStream getPropertiesStream() throws IOException {
        File jarPath=new File(ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String propertiesPath=jarPath.getParentFile().getAbsolutePath();

        InputStream inputStream = new FileInputStream(propertiesPath + File.separator + "config.properties");

        return inputStream;
    }

    public static String getPlatformToolsFolder() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = ConfigManager.getPropertiesStream();
        prop.load(inputStream);

        return prop.getProperty("platformToolsFolder");
    }

    public static String getPowerProfileFile() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = ConfigManager.getPropertiesStream();
        prop.load(inputStream);

        return prop.getProperty("powerProfileFile");
    }

    public static int getMaxRun() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = ConfigManager.getPropertiesStream();
        prop.load(inputStream);

        return Integer.parseInt(prop.getProperty("maxRun"));
    }

    public static int getInteractions() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = ConfigManager.getPropertiesStream();
        prop.load(inputStream);

        return Integer.parseInt(prop.getProperty("interactions"));
    }

    public static int getTimeBetweenInteractions() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = ConfigManager.getPropertiesStream();
        prop.load(inputStream);

        return Integer.parseInt(prop.getProperty("timeBetweenInteractions"));
    }
}
