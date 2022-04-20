package com.florian.vertibayes.webservice.domain.external;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.vertibayes.bayes.Bin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebNode {
    //Simplified Class for WebRequests that require communicating about Nodes
    private List<String> parents = new ArrayList<>();
    private String name;
    private Attribute.AttributeType type;
    private List<WebTheta> probabilities = new ArrayList<>();
    private Set<Bin> bins = new HashSet<>();

    public Set<Bin> getBins() {
        return bins;
    }

    public void setBins(Set<Bin> bins) {
        this.bins = bins;
    }

    public List<WebTheta> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(List<WebTheta> probabilities) {
        this.probabilities = probabilities;
    }

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Attribute.AttributeType getType() {
        return type;
    }

    public void setType(Attribute.AttributeType type) {
        this.type = type;
    }
}
