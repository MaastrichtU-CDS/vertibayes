package com.florian.vertibayes.webservice.domain.external;

import weka.classifiers.bayes.BayesNet;

public class ExpectationMaximizationTestResponse extends ExpectationMaximizationResponse {
    private BayesNet weka;

    public BayesNet getWeka() {
        return weka;
    }

    public void setWeka(BayesNet weka) {
        this.weka = weka;
    }
}
