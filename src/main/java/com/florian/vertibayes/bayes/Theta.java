package com.florian.vertibayes.bayes;

import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;

import java.util.ArrayList;
import java.util.List;

public class Theta {
    private AttributeRequirement localRequirement;
    private List<ParentValue> parents = new ArrayList<>();
    private double p;
    private boolean calculated; //help variable to speed things up

    public Theta() {
        calculated = false;
    }

    public void calculated() {
        calculated = true;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public AttributeRequirement getLocalRequirement() {
        return localRequirement;
    }

    public void setLocalRequirement(AttributeRequirement localRequirement) {
        this.localRequirement = localRequirement;
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
