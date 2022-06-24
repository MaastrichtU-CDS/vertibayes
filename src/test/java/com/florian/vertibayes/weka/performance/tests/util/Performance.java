package com.florian.vertibayes.weka.performance.tests.util;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Performance {
    private double realAuc;
    private double syntheticAuc;
    private double syntheticFoldAuc;
    private double wekaAuc;
    private String name;

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
}
