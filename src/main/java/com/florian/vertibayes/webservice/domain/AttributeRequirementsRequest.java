package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.bayes.data.Attribute;

import java.util.List;

public class AttributeRequirementsRequest {
    List<Attribute> requirements;

    public AttributeRequirementsRequest() {
    }

    public List<Attribute> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Attribute> requirements) {
        this.requirements = requirements;
    }
}
