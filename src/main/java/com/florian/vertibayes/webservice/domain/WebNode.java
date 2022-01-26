package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.bayes.data.Attribute;

import java.util.List;

public class WebNode {
    List<String> parents;
    String name;
    Attribute.AttributeType type;

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
