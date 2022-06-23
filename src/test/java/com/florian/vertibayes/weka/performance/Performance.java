package com.florian.vertibayes.weka.performance;

public class Performance {
    private double realAuc;
    private double syntheticAuc;
    private double syntheticFoldAuc;
    private double wekaAuc;

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
}
