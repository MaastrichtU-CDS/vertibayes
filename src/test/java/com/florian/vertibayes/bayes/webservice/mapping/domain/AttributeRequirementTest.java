package com.florian.vertibayes.bayes.webservice.mapping.domain;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttributeRequirementTest {

    @Test
    public void testRequirementsReal() {
        Attribute low = new Attribute(Attribute.AttributeType.real, "-1", "low");
        Attribute medium = new Attribute(Attribute.AttributeType.real, "0.5", "medium");
        Attribute high = new Attribute(Attribute.AttributeType.real, "3", "high");

        Attribute upperLimit = new Attribute(Attribute.AttributeType.real, "2", "upperLimit");
        Attribute lowerLimit = new Attribute(Attribute.AttributeType.real, "0", "lowerLimit");

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

    @Test
    public void testRequirementsNumeric() {
        Attribute low = new Attribute(Attribute.AttributeType.numeric, "-2", "low");
        Attribute medium = new Attribute(Attribute.AttributeType.numeric, "1", "medium");
        Attribute high = new Attribute(Attribute.AttributeType.numeric, "3", "high");

        Attribute upperLimit = new Attribute(Attribute.AttributeType.numeric, "2", "upperLimit");
        Attribute lowerLimit = new Attribute(Attribute.AttributeType.numeric, "0", "lowerLimit");

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