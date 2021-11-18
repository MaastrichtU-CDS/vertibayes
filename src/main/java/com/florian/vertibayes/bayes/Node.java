package com.florian.vertibayes.bayes;

import com.florian.vertibayes.bayes.data.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Node {
    private List<Node> parents = new ArrayList<>();
    private Set<String> uniquevalues;
    private String name;
    private Attribute.AttributeType type;
    private List<Theta> probabilities = new ArrayList<>();

    public Node() {
    }

    public List<Theta> getProbabilities() {
        return probabilities;
    }

    public Node(String name, Set<String> uniquevalues, Attribute.AttributeType type) {
        this.name = name;
        this.uniquevalues = uniquevalues;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Attribute.AttributeType getType() {
        return type;
    }

    public Set<String> getUniquevalues() {
        return uniquevalues;
    }

    public List<Node> getParents() {
        return parents;
    }

    public void setParents(List<Node> parents) {
        this.parents = parents;
    }

    public void setUniquevalues(Set<String> uniquevalues) {
        this.uniquevalues = uniquevalues;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Attribute.AttributeType type) {
        this.type = type;
    }
}
