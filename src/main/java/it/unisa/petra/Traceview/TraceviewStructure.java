package it.unisa.petra.Traceview;

import java.util.List;

/**
 * @author dardin88
 */
public class TraceviewStructure {

    private final List<TraceLine> tracelines;
    private final int startTime;
    private final int endTime;

    TraceviewStructure(List<TraceLine> tracelines, int startTime, int endTime) {
        this.tracelines = tracelines;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public List<TraceLine> getTraceLines() {
        return tracelines;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }
}
