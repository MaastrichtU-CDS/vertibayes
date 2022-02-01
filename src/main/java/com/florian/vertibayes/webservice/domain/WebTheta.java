package com.florian.vertibayes.webservice.domain;

import java.util.Map;

public class WebTheta {
    private WebValue localValue;
    private Map<String, WebValue> parentValues;
    private double p;

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public WebValue getLocalValue() {
        return localValue;
    }

    public void setLocalValue(WebValue localValue) {
        this.localValue = localValue;
    }

    public Map<String, WebValue> getParentValues() {
        return parentValues;
    }

    public void setParentValues(Map<String, WebValue> parentValues) {
        this.parentValues = parentValues;
    }
}
