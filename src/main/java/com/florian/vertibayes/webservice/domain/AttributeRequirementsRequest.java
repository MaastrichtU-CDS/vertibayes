package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.bayes.data.Attribute;

import java.util.List;

public class AttributeRequirementsRequest {
    private List<Attribute> requirements;
    private List<AttributeRequirement> requirements2;

    public AttributeRequirementsRequest() {
    }

    public List<AttributeRequirement> getRequirements2() {
        return requirements2;
    }

    public void setRequirements2(List<AttributeRequirement> requirements2) {
        this.requirements2 = requirements2;
    }

    public List<Attribute> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Attribute> requirements) {
        this.requirements = requirements;
    }

}
