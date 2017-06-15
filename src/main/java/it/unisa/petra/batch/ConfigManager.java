package it.unisa.petra.batch;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Dario
 */
class ConfigManager {

    private final String propertiesPath;

    ConfigManager(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    private InputStream getPropertiesStream() throws IOException {
        return new FileInputStream(propertiesPath);
    }

    String getPowerProfileFile() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("powerProfileFile");
    }

    int getRuns() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return Integer.parseInt(prop.getProperty("runs"));
    }

    int getTrials() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return Integer.parseInt(prop.getProperty("trials"));
    }

    int getInteractions() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return Integer.parseInt(prop.getProperty("interactions"));
    }

    int getTimeBetweenInteractions() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return Integer.parseInt(prop.getProperty("timeBetweenInteractions"));
    }

    String getApkLocationPath() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("apkLocation");
    }

    String getOutputLocation() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("outputLocation");
    }

    String getScriptLocationPath() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("scriptLocationPath");
    }

    String getScriptTime() throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = this.getPropertiesStream()) {
            prop.load(inputStream);
        }

        return prop.getProperty("scriptTime");
    }
}
