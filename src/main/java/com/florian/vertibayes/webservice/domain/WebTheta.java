package com.florian.vertibayes.webservice.domain;

import java.util.Map;

public class WebTheta {
    private String localValue;
    private Map<String, String> parentValues;
    private double p;

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public String getLocalValue() {
        return localValue;
    }

    public void setLocalValue(String localValue) {
        this.localValue = localValue;
    }

    public Map<String, String> getParentValues() {
        return parentValues;
    }

    public void setParentValues(Map<String, String> parentValues) {
        this.parentValues = parentValues;
    }
}
