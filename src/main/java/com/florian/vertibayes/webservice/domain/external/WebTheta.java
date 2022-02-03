package com.florian.vertibayes.webservice.domain.external;

import com.florian.vertibayes.bayes.ParentValue;

import java.util.ArrayList;
import java.util.List;

public class WebTheta {
    private WebValue localValue;
    private List<ParentValue> parentValues = new ArrayList<>();
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

    public List getParentValues() {
        return parentValues;
    }

    public void setParentValues(List parentValues) {
        this.parentValues = parentValues;
    }
}
