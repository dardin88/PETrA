package it.unisa.petra.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dardin88
 */
class BoxplotData {

    private final String signature;
    private final List<Double> values;

    BoxplotData(String signature) {
        this.signature = signature;
        this.values = new ArrayList<>();
    }

    String getSignature() {
        return signature;
    }

    List<Double> getValues() {
        return values;
    }

    void addValue(double value) {
        this.values.add(value);
    }

}
