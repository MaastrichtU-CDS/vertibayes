package com.florian.vertibayes.bayes;

import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirement;

import java.util.*;

public class Node {
    private List<Node> parents = new ArrayList<>();
    private Set<String> uniquevalues = new HashSet<>();
    private String name;
    private Attribute.AttributeType type;
    private List<Theta> probabilities = new ArrayList<>();
    private Set<Bin> bins = new HashSet<>();
    private boolean discrete = true;

    public Node() {
    }

    public Set<Bin> getBins() {
        return bins;
    }

    public void setBins(Set<Bin> bins) {
        this.bins = bins;
    }

    public boolean isDiscrete() {
        return discrete;
    }

    public void setDiscrete(boolean discrete) {
        this.discrete = discrete;
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


    public static List<Theta> findSliblings(Theta t, Node node) {
        List<Theta> sliblings = new ArrayList<>();
        Map<String, AttributeRequirement> parentValues = new HashMap<>();
        if (node.getParents().size() == 0) {
            //no parents, so all thetas are sliblings
            sliblings.addAll(node.getProbabilities());
        } else {
            // collect parent values
            for (ParentValue parent : t.getParents()) {
                String key = parent.getName();
                if (parent.getRequirement().isRange()) {
                    key += parent.getRequirement().getLowerLimit().getValue();
                } else {
                    key += parent.getRequirement().getValue().getValue();
                }
                parentValues.put(key, parent.getRequirement());
            }
            for (Theta theta : node.getProbabilities()) {
                boolean correctTheta = true;
                for (ParentValue p : theta.getParents()) {
                    String key = p.getName();
                    if (p.getRequirement().isRange()) {
                        key += p.getRequirement().getLowerLimit().getValue();
                    } else {
                        key += p.getRequirement().getValue().getValue();
                    }
                    if (!p.getRequirement().equals(parentValues.get(key))) {
                        correctTheta = false;
                    }
                }
                if (correctTheta) {
                    sliblings.add(theta);
                }
            }
        }
        return sliblings;
    }
}
