package com.florian.vertibayes.bayes;

import com.florian.vertibayes.bayes.data.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Node {
    private List<Node> parents = new ArrayList<>();
    private List<Node> children = new ArrayList<>();
    private Set<String> uniquevalues;
    private String name;
    private Attribute.AttributeType type;
    private List<Theta> probabilities = new ArrayList<>();

    public Node() {
    }

    public List<Theta> getProbabilities() {
        return probabilities;
    }

    public void initProbabilities() {
        for (String s : uniquevalues) {
            Theta t = new Theta();
            t.setLocalValue(new Attribute(type, s, name));
            List<ParentValue> list = new ArrayList<>();
            Random r = new Random();
            t.setP(r.nextDouble());
            if (parents.size() > 0) {
                for (Node n : parents) {
                    for (String u : n.getUniquevalues()) {
                        ParentValue p = new ParentValue();
                        p.setName(n.getName());
                        p.setValue(new Attribute(n.getType(), u, n.getName()));
                        list.add(p);
                    }
                }
            }
            t.setParents(list);
        }
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

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
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
