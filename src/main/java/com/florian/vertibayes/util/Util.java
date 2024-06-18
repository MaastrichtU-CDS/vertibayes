package com.florian.vertibayes.util;

import com.florian.vertibayes.bayes.Bin;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public final class Util {

    private Util() {
    }

    public static Set<Bin> mapBins(Set<Bin> input) {
        // Utility function that exists because Sets apparently don't get mapped nicely from JSON cuz of reasons
        // For some reason it is then a linkeHashMap; but only when running as a webservice.
        // This also makes it a fucking disaster to test

        Set<Bin> bins = new HashSet<>();
        try {
            Bin b = (Bin) input.toArray()[0];
            for (Bin in : input) {
                bins.add(in);
            }
        } catch (Exception e) {
            for (Object in : input) {
                Bin b = new Bin();
                b.setUpperLimit((String) ((LinkedHashMap) input.toArray()[0]).get("upperLimit"));
                b.setLowerLimit((String) ((LinkedHashMap) input.toArray()[0]).get("lowerLimit"));
                bins.add(b);
            }
        }
        return bins;

    }
}
