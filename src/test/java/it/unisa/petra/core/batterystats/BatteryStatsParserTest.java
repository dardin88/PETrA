package it.unisa.petra.core.batterystats;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class BatteryStatsParserTest {

    @Test
    public void testParser() throws Exception {
        String fileName = "src/test/resources/batterystats_example";
        int traceviewStart = 0;
        ArrayList<EnergyInfo> result = BatteryStatsParser.parseFile(fileName, traceviewStart);
        assertEquals(3, result.size());
        assertEquals(5, result.get(0).getDevices().size());
        assertEquals(6, result.get(1).getDevices().size());
        assertEquals(6, result.get(2).getDevices().size());
    }

}
