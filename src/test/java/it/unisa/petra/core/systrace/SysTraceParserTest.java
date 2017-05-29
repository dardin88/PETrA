package it.unisa.petra.core.systrace;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class SysTraceParserTest {

    @Test
    public void testParser1() throws Exception {
        String fileName = "src/test/resources/systrace_example";
        int traceviewStart = 0;
        int traceviewLength = 10000;
        SysTrace result = SysTraceParser.parseFile(fileName, traceviewStart, traceviewLength);
        assertEquals(25123208, result.getSystraceStartTime());
        assertEquals(25123208, result.getFrequencies().get(0).getTime());
        assertEquals(486000, result.getFrequencies().get(0).getValue());
    }

    @Test
    public void testParser2() throws Exception {
        String fileName = "src/test/resources/systrace_example_2";
        int traceviewStart = 0;
        int traceviewLength = 30000;
        SysTrace result = SysTraceParser.parseFile(fileName, traceviewStart, traceviewLength);
        assertEquals(3541836, result.getSystraceStartTime());
        assertEquals(3541836, result.getFrequencies().get(0).getTime());
        assertEquals(0, result.getFrequencies().get(0).getValue());
        assertEquals(0, result.getFrequencies().get(0).getCore());
        assertEquals(3542010, result.getFrequencies().get(1).getTime());
        assertEquals(702000, result.getFrequencies().get(1).getValue());
        assertEquals(0, result.getFrequencies().get(1).getCore());
    }

}
