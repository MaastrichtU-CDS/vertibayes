package com.florian.vertibayes.weka;

public class PerformanceTuple {
    private double realAuc;
    private double syntheticAuc;

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
