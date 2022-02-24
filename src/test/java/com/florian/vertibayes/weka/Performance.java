package com.florian.vertibayes.weka;

public class Performance {
    private double realAuc;
    private double syntheticAuc;
    private double syntheticFoldAuc;

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
}
