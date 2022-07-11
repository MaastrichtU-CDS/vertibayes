package com.florian.vertibayes.weka.performance.tests.util;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Performance {
    private double realAuc;
    private double syntheticAuc;
    private double syntheticFoldAuc;
    private double wekaAuc;
    private String name;
    private Map<String, List<Integer>> errors = new HashMap<>();
    private Map<String, List<Integer>> wekaErrors = new HashMap<>();
    private int[] uniqueErrors = new int[3]; // 0 = fed, 1 = weka, 2 = error in both

    public int[] getUniqueErrors() {
        return uniqueErrors;
    }

    public void setUniqueErrors(int[] uniqueErrors) {
        this.uniqueErrors = uniqueErrors;
    }

    public Map<String, List<Integer>> getWekaErrors() {
        return wekaErrors;
    }

    public void setWekaErrors(Map<String, List<Integer>> wekaErrors) {
        this.wekaErrors = wekaErrors;
    }

    public Map<String, List<Integer>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<Integer>> errors) {
        this.errors = errors;
    }

    public double getSyntheticFoldAuc() {
        return syntheticFoldAuc;
    }

    public void setSyntheticFoldAuc(double syntheticFoldAuc) {
        this.syntheticFoldAuc = syntheticFoldAuc;
    }

    public double getRealAuc() {
        return realAuc;
    }

    public void setRealAuc(double realAuc) {
        this.realAuc = realAuc;
    }

    public double getSyntheticAuc() {
        return syntheticAuc;
    }

    public void setSyntheticAuc(double syntheticAuc) {
        this.syntheticAuc = syntheticAuc;
    }

    public double getWekaAuc() {
        return wekaAuc;
    }

    public void setWekaAuc(double wekaAuc) {
        this.wekaAuc = wekaAuc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Performance averagePerformance(List<Performance> performances) {
        Performance p = new Performance();
        double realAuc = 0;
        double syntheticAuc = 0;
        double syntheticFoldAuc = 0;
        for (Performance per : performances) {
            realAuc += per.getRealAuc();
            syntheticAuc += per.getSyntheticAuc();
            syntheticFoldAuc += per.getSyntheticFoldAuc();
            p.getErrors().putAll(per.getErrors());
        }
        p.setRealAuc(realAuc / performances.size());
        p.setSyntheticAuc(syntheticAuc / performances.size());
        p.setSyntheticFoldAuc(syntheticFoldAuc / performances.size());
        return p;
    }

    public static void checkVariance(List<Performance> performances, Performance average, Variance variance) {
        for (Performance p : performances) {
            assertEquals(average.getRealAuc(), p.getRealAuc(), variance.getRealAucVariance());
            assertEquals(average.getSyntheticAuc(), p.getSyntheticAuc(), variance.getSyntheticAucVariance());
            assertEquals(average.getSyntheticFoldAuc(), p.getSyntheticFoldAuc(),
                         variance.getSyntheticFoldAucVariance());
        }
    }

    public void matchErrors() {
        for (String key : errors.keySet()) {
            List<Integer> fedError = errors.get(key);
            List<Integer> wekaError = wekaErrors.get(key.replace(".csv", "WEKA.arff"));
            Set<Integer> errors = new HashSet<>();
            for (int i = 0; i < fedError.size(); i++) {
                int error = fedError.get(i);
                if (!errors.contains(error)) {
                    errors.add(error);
                    if (wekaError.contains(error)) {
                        uniqueErrors[2]++;
                    } else {
                        uniqueErrors[0]++;
                    }
                }
            }
            for (int i = 0; i < wekaError.size(); i++) {
                int error = wekaError.get(i);
                if (!errors.contains(error)) {
                    errors.add(error);
                    if (!fedError.contains(error)) {
                        uniqueErrors[1]++;
                    }
                }
            }
        }
    }
}
