package com.florian.vertibayes.bayes;

import com.florian.vertibayes.bayes.data.Attribute;

public class ParentValue {
    private String name;
    private Attribute value;

    public ParentValue() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Attribute getValue() {
        return value;
    }

    public void setValue(Attribute value) {
        this.value = value;
    }
}
