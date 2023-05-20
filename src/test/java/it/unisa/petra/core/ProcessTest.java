package it.unisa.petra.core;

import it.unisa.petra.core.powerprofile.PowerProfile;
import it.unisa.petra.core.powerprofile.PowerProfileParser;
import it.unisa.petra.core.traceview.TraceLine;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class ProcessTest {

    @Test
    public void testParser() throws Exception {
        String traceviewFilename = "src/test/resources/tracedump_example";
        String batteryStatsFilename = "src/test/resources/batterystats_example";
        String systrace_example = "src/test/resources/systrace_example";
        String powerProfileFile = "src/test/resources/power_profile_example.xml";

        String appName = "ch.smalltech.battery";

        Process process = new Process();

        PowerProfile powerProfile = PowerProfileParser.parseFile(powerProfileFile);

        List<TraceLine> traceLines = process.parseAndAggregateResults(traceviewFilename, batteryStatsFilename, systrace_example, powerProfile, appName, 1);

        assertEquals(0.000296518, traceLines.get(0).getTimeLength(), 0.000001);
        assertEquals(0.000359394, traceLines.get(0).getConsumption(), 0.0000001);
    }

}
