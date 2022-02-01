package com.florian.vertibayes.bayes;

import com.florian.vertibayes.webservice.domain.AttributeRequirement;

public class ParentValue {
    private String name;
    private AttributeRequirement requirement;

    public ParentValue() {
    }

    public AttributeRequirement getRequirement() {
        return requirement;
    }

    public void setRequirement(AttributeRequirement requirement) {
        this.requirement = requirement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
