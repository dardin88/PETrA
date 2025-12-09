package it.unisa.petra.core.perfetto;

import org.junit.Test;
import java.io.IOException;

public class PerfettoJsonParserTest {

    @Test
    public void testPrintAllProcessNames() throws IOException {
        // Use a sample Perfetto JSON file (should be created for real tests)
        String jsonFilePath = "src/test/resources/perfetto_example.json";
        PerfettoJsonParser parser = new PerfettoJsonParser(jsonFilePath);
        parser.printAllProcessNames();
        // No assertion: just ensure no exceptions and output is printed
    }

    @Test
    public void testPrintAllSlices() throws IOException {
        // Use a sample Perfetto JSON file (should be created for real tests)
        String jsonFilePath = "src/test/resources/perfetto_example.json";
        PerfettoJsonParser parser = new PerfettoJsonParser(jsonFilePath);
        parser.printAllSlices();
        // No assertion: just ensure no exceptions and output is printed
    }
}
