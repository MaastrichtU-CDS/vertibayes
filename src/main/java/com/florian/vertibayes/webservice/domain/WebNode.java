package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.bayes.data.Attribute;

import java.util.List;

public class WebNode {
    //Simplified Class for WebRequests that require communicating about Nodes
    private List<String> parents;
    private String name;
    private Attribute.AttributeType type;
    private List<WebTheta> probabilities;

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
