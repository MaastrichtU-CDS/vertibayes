package com.florian.vertibayes.weka.performance.tests.util;

public class Variance {
    private double realAucVariance;
    private double syntheticAucVariance;
    private double syntheticFoldAucVariance;

    public double getSyntheticFoldAucVariance() {
        return syntheticFoldAucVariance;
    }

    public void setSyntheticFoldAucVariance(double syntheticFoldAucVariance) {
        this.syntheticFoldAucVariance = syntheticFoldAucVariance;
    }

    public double getRealAucVariance() {
        return realAucVariance;
    }

    public void setRealAucVariance(double realAucVariance) {
        this.realAucVariance = realAucVariance;
    }

    public double getSyntheticAucVariance() {
        return syntheticAucVariance;
    }

    public void setSyntheticAucVariance(double syntheticAucVariance) {
        this.syntheticAucVariance = syntheticAucVariance;
    }
}
