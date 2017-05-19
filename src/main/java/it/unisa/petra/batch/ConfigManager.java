package it.unisa.petra.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Dario
 */
class ConfigManager {

    private static InputStream getPropertiesStream() throws IOException {
        File jarPath = new File(ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String propertiesPath = jarPath.getParentFile().getAbsolutePath();

        return new FileInputStream(propertiesPath + File.separator + "config.properties");
    }

    static String getPowerProfileFile() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("powerProfileFile");
    }

    static int getRuns() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return Integer.parseInt(prop.getProperty("runs"));
    }

    static int getTrials() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return Integer.parseInt(prop.getProperty("trials"));
    }

    static int getInteractions() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return Integer.parseInt(prop.getProperty("interactions"));
    }

    static int getTimeBetweenInteractions() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return Integer.parseInt(prop.getProperty("timeBetweenInteractions"));
    }

    static String getAppName() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("appName");
    }

    static String getApkLocationPath() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("apkLocation");
    }

    static String getOutputLocation() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("outputLocation");
    }

    static String getScriptLocationPath() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("scriptLocationPath");
    }

    static String getSDKLocationPath() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = ConfigManager.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("sdkLocationPath");
    }
}
