package it.unisa.petra.core.perfetto;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Parses Perfetto JSON trace output (from traceconv json) and extracts relevant data.
 */
public class PerfettoJsonParser {

    private final JsonObject root;

    public PerfettoJsonParser(String jsonFilePath) throws IOException {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            this.root = new Gson().fromJson(reader, JsonObject.class);
        }
    }

    /**
     * Example: Extract all process names from the trace.
     */
    public void printAllProcessNames() {
        JsonArray processes = root.getAsJsonObject("metadata").getAsJsonArray("processes");
        for (JsonElement elem : processes) {
            JsonObject proc = elem.getAsJsonObject();
            System.out.println(proc.get("name").getAsString());
        }
    }

    /**
     * Example: Extract all slices/events of a given track.
     */
    public void printAllSlices() {
        JsonArray slices = root.getAsJsonArray("slices");
        if (slices != null) {
            for (JsonElement elem : slices) {
                JsonObject slice = elem.getAsJsonObject();
                System.out.println(slice);
            }
        }
    }

    // Add more methods to extract CPU, thread, or custom event data as needed.
}
