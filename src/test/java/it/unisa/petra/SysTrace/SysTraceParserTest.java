package it.unisa.petra.SysTrace;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author dardin88
 */
public class SysTraceParserTest {

    @Test
    public void testParser() throws Exception {
        String fileName = "src/test/resources/systrace";
        int traceviewStart = 0;
        int traceviewLength = 10000;
        SysTrace result = SysTraceParser.parseFile(fileName, traceviewStart, traceviewLength);
        assertEquals(25123208, result.getSystraceStartTime());
        assertEquals(25123208, result.getFrequency().get(0).getTime());
        assertEquals(486000, result.getFrequency().get(0).getValue());
    }

}
