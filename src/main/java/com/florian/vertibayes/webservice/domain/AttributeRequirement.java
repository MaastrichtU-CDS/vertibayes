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
        if (attribute.isUknown()) {
            return expectsUnknown();
        } else if (isRange()) {
            return fitsWithinLimits(attribute);
        } else {
            return attribute.equals(value);
        }
    }

    public boolean expectsUnknown() {
        if (isRange()) {
            return lowerLimit.isUknown();
        } else {
            return value.isUknown();
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
        if (!isRange()) {
            return value.getAttributeName();
        } else {
            return lowerLimit.getAttributeName();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof AttributeRequirement)) {
            return false;
        }
        AttributeRequirement attributeReq = (AttributeRequirement) o;
        if (!this.isRange()) {
            return this.isRange() == attributeReq.isRange() && this.value.equals(attributeReq.value);
        } else {
            return this.isRange() == attributeReq.isRange() && this.upperLimit.equals(
                    attributeReq.upperLimit) && this.lowerLimit.equals(attributeReq.lowerLimit);
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        final int prime = 5;

        hash = prime * hash + (value == null ? 0 : value.hashCode());
        hash = prime * hash + (upperLimit == null ? 0 : upperLimit.hashCode());
        hash = prime * hash + (lowerLimit == null ? 0 : lowerLimit.hashCode());
        hash = prime * hash + (((Boolean) range).hashCode());
        return hash;
    }
}
