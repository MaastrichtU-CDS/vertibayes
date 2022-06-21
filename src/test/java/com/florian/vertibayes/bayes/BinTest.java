package com.florian.vertibayes.bayes;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BinTest {
    @Test
    public void testBins() {
        Bin bin1 = new Bin();
        bin1.setUpperLimit("1");
        bin1.setLowerLimit("2");

        Bin bin2 = new Bin();
        bin2.setUpperLimit("1");
        bin2.setLowerLimit("3");

        Bin bin3 = new Bin();
        bin3.setUpperLimit("2");
        bin3.setLowerLimit("2");

        Bin bin4 = new Bin();
        bin4.setUpperLimit("1");
        bin4.setLowerLimit("2");

        Set<Bin> set = new HashSet<>();
        Set<Bin> set2 = new HashSet<>();
        set.add(bin1);
        set2.add(bin4);
        set.addAll(set);
        assertTrue(set.contains(bin1));
        assertFalse(set.contains(bin2));
        assertFalse(set.contains(bin3));
        assertTrue(set.contains(bin4));
    }

}