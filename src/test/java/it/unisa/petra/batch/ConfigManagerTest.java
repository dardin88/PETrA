package it.unisa.petra.batch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class ConfigManagerTest {

    @Test
    public void testConfigManager1() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals("/home/dardin88/Desktop/energy_consumption_bad_smell/test_data/power_profile_mako.xml", configManager.getPowerProfileFile());
    }

    @Test
    public void testConfigManager2() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals(10, configManager.getRuns());
    }

    @Test
    public void testConfigManager3() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals(1500, configManager.getInteractions());
    }

    @Test
    public void testConfigManager4() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals(200, configManager.getTimeBetweenInteractions());
    }

    @Test
    public void testConfigManager5() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals("a2dp.Vol", configManager.getAppName());
    }

    @Test
    public void testConfigManager6() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals("/home/dardin88/test_data/a2dp.Vol/", configManager.getOutputLocation());
    }

    @Test
    public void testConfigManager7() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals(5, configManager.getTrials());
    }

    @Test
    public void testConfigManager8() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals("/home/dardin88/a2dp.Vol.apk", configManager.getApkLocationPath());
    }

    @Test
    public void testConfigManager9() throws Exception {
        ConfigManager configManager = new ConfigManager("src/test/resources/config.properties");
        assertEquals("/home/dardin88/test_data/a2dp.Vol_scripts/", configManager.getScriptLocationPath());
    }
}
