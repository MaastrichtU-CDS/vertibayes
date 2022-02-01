package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.bayes.data.Attribute;

public class AttributeRequirement {
    // Class used to communicate requirements
    // If value is used it is a direct comparison, so requirement is fullfilled if value == attribute
    // If range is used then lower <= attribue < upperLimit

    private Attribute value;
    private boolean range;
    private Attribute upperLimit;
    private Attribute lowerLimit;

    public AttributeRequirement() {
    }

    public AttributeRequirement(Attribute value) {
        this.range = false;
        this.value = value;
    }

    public AttributeRequirement(Attribute lowerLimit, Attribute upperLimit) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.range = true;
    }

    public boolean checkRequirement(Attribute attribute) {
        if (isRange()) {
            return fitsWithinLimits(attribute);
        } else {
            return attribute.equals(value);
        }
    }

    private boolean fitsWithinLimits(Attribute attribute) {
        // attribute needs to be higher or equal to lower limit and lower than the upper limit
        if (lowerLimit.compareTo(attribute) > 0) {
            return false;
        } else if (upperLimit.compareTo(attribute) <= 0) {
            return false;
        }
        return true;
    }

    public Attribute getValue() {
        return value;
    }

    public void setValue(Attribute value) {
        this.value = value;
    }

    public boolean isRange() {
        return range;
    }

    public void setRange(boolean range) {
        this.range = range;
    }

    public Attribute getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Attribute upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Attribute getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Attribute lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public String getName() {
        if (isRange()) {
            return value.getAttributeName();
        } else {
            return lowerLimit.getAttributeName();
        }
    }
}
