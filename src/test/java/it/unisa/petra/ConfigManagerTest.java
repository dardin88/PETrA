package it.unisa.petra;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class ConfigManagerTest {

    @Test
    public void testConfigManager1() throws Exception {
        assertEquals("/home/dardin88/android-sdk-linux/platform-tools/", ConfigManager.getPlatformToolsFolder());
    }

    @Test
    public void testConfigManager2() throws Exception {
        assertEquals("/home/dardin88/android-sdk-linux/tools/", ConfigManager.getToolsFolder());
    }

    @Test
    public void testConfigManager3() throws Exception {
        assertEquals("/home/dardin88/Desktop/energy_consumption_bad_smell/test_data/power_profile_mako.xml", ConfigManager.getPowerProfileFile());
    }

    @Test
    public void testConfigManager4() throws Exception {
        assertEquals(10, ConfigManager.getMaxRun());
    }

    @Test
    public void testConfigManager5() throws Exception {
        assertEquals(1500, ConfigManager.getInteractions());
    }

    @Test
    public void testConfigManager6() throws Exception {
        assertEquals(200, ConfigManager.getTimeBetweenInteractions());
    }

    @Test
    public void testConfigManager7() throws Exception {
        assertEquals("a2dp.Vol", ConfigManager.getAppName());
    }

    @Test
    public void testConfigManager8() throws Exception {
        assertEquals("a2dp.Vol.apk", ConfigManager.getApkName());
    }

    @Test
    public void testConfigManager9() throws Exception {
        assertEquals("/home/dardin88/a2dp.Vol.apk", ConfigManager.getApkLocation());
    }

    @Test
    public void testConfigManager10() throws Exception {
        assertEquals("/home/dardin88/test_data/a2dp.Vol/", ConfigManager.getOutputLocation());
    }
}
