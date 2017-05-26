package it.unisa.petra.core.powerprofile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class PowerProfileParserTest {

    @Test
    public void testParser1() throws Exception {
        String fileName = "src/test/resources/power_profile_example.xml";
        PowerProfile powerProfile = PowerProfileParser.parseFile(fileName);
        assertEquals(47.05, powerProfile.getCpuConsumptionByFrequency(0, 384000), 0.00001);
        assertEquals(62.09, powerProfile.getDevices().get("wifi.active"), 0.00001);
    }

    @Test
    public void testParser2() throws Exception {
        String fileName = "src/test/resources/power_profile_w_clusters.xml";
        PowerProfile powerProfile = PowerProfileParser.parseFile(fileName);
        assertEquals(0, powerProfile.getClusterByCore(3));
        assertEquals(1, powerProfile.getClusterByCore(5));
        assertEquals(98, powerProfile.getCpuConsumptionByFrequency(0, 1400000), 0.00001);
        assertEquals(362, powerProfile.getCpuConsumptionByFrequency(1, 1896000), 0.00001);
        assertEquals(4.3, powerProfile.getDevices().get("cpu.idle"), 0.00001);
    }

}
