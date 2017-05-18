package it.unisa.petra.BatteryStats;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class BatteryStatsParserTest {

    @Test
    public void testNumberOfEnergyInfo() throws Exception {
        String fileName = "src/test/resources/batterystats_example";
        int traceviewStart = 0;
        int traceviewLength = 10000;
        ArrayList<EnergyInfo> result = BatteryStatsParser.parseFile(fileName, traceviewStart, traceviewLength);
        assertEquals(3, result.size());
        assertEquals(5, result.get(0).getDevices().size());
        assertEquals(6, result.get(1).getDevices().size());
        assertEquals(6, result.get(2).getDevices().size());
    }

}
