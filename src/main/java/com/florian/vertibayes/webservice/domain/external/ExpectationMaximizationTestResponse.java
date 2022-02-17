package com.florian.vertibayes.webservice.domain.external;

import weka.classifiers.bayes.BayesNet;

public class ExpectationMaximizationTestResponse extends ExpectationMaximizationResponse {
    private BayesNet weka;
    private double syntheticAuc;

    public double getSyntheticAuc() {
        return syntheticAuc;
    }

    public void setSyntheticAuc(double syntheticAuc) {
        this.syntheticAuc = syntheticAuc;
    }

    public BayesNet getWeka() {
        return weka;
    }

    public void setWeka(BayesNet weka) {
        this.weka = weka;
    }
}
