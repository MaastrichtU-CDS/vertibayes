package com.florian.vertibayes.bayes.webservice.mapping.domain;

import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttributeRequirementTest {

    @Test
    public void testRequirements() {
        Attribute low = new Attribute(Attribute.AttributeType.number, "-1", "low");
        Attribute medium = new Attribute(Attribute.AttributeType.number, "0.5", "medium");
        Attribute high = new Attribute(Attribute.AttributeType.number, "3", "high");

        Attribute upperLimit = new Attribute(Attribute.AttributeType.number, "2", "upperLimit");
        Attribute lowerLimit = new Attribute(Attribute.AttributeType.number, "0", "lowerLimit");

        AttributeRequirement comparison = new AttributeRequirement(medium);
        AttributeRequirement range = new AttributeRequirement(lowerLimit, upperLimit);

        assertEquals(comparison.checkRequirement(low), false);
        assertEquals(comparison.checkRequirement(medium), true);
        assertEquals(comparison.checkRequirement(high), false);

        assertEquals(range.checkRequirement(low), false);
        assertEquals(range.checkRequirement(medium), true);
        assertEquals(range.checkRequirement(high), false);
        assertEquals(range.checkRequirement(upperLimit), false);
        assertEquals(range.checkRequirement(lowerLimit), true);
    }
}