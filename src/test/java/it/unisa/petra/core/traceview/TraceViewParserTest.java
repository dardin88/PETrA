package it.unisa.petra.core.traceview;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class TraceViewParserTest {

    @Test
    public void testParser() throws Exception {
        String fileName = "src/test/resources/tracedump_example";
        String appName = "ch.smalltech.battery";
        TraceviewStructure result = TraceViewParser.parseFile(fileName, appName);
        TraceLine firstTraceLine = result.getTraceLines().get(0);
        assertEquals("ch.smalltech.battery.core.components.BatteryView$1.handleMessage (Landroid/os/Message;)V\tBatteryView.java", firstTraceLine.getSignature());
        assertEquals(7, firstTraceLine.getEntrance());
        assertEquals(7, firstTraceLine.getExit());
        assertEquals(0, result.getStartTime());
        assertEquals(12964, result.getEndTime());
    }

}
