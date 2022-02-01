package com.florian.vertibayes.bayes;

import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirement;

import java.util.ArrayList;
import java.util.List;

public class Theta {
    private Attribute localValue;
    private AttributeRequirement localRequirement;
    private List<ParentValue> parents = new ArrayList<>();
    private double p;

    public Theta() {
    }

    public AttributeRequirement getLocalRequirement() {
        return localRequirement;
    }

    public void setLocalRequirement(AttributeRequirement localRequirement) {
        this.localRequirement = localRequirement;
    }

    public Attribute getLocalValue() {
        return localValue;
    }

    public void setLocalValue(Attribute localValue) {
        this.localValue = localValue;
    }

    public List<ParentValue> getParents() {
        return parents;
    }

    public void setParents(List<ParentValue> parents) {
        this.parents = parents;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }


}
