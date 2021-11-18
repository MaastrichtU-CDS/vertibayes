package com.florian.vertibayes.bayes.data;

import org.junit.jupiter.api.Test;

import static com.florian.vertibayes.bayes.data.Parser.parseCsv;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataTest {

    @Test
    public void testCreateNetwork() {
        Data d = parseCsv("resources/smallK2Example_firsthalf.csv", 0);
        assertEquals(d.getIndividualRow("1"), 0);

    }
}