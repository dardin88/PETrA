package it.unisa.petra.ui;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dardin88
 */
public class BoxplotData {

    private final String signature;
    private final List<Double> values;

    public BoxplotData(String signature) {
        this.signature = signature;
        this.values = new ArrayList<>();
    }

    public String getSignature() {
        return signature;
    }

    public List<Double> getValues() {
        return values;
    }

    public void addValue(double value) {
        this.values.add(value);
    }

}
