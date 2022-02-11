package com.florian.vertibayes.webservice.domain.external;

import weka.classifiers.bayes.BayesNet;

import java.util.List;

public class ExpectationMaximizationResponse {
    private List<WebNode> nodes;
    private BayesNet weka;

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }

    public BayesNet getWeka() {
        return weka;
    }

    public void setWeka(BayesNet weka) {
        this.weka = weka;
    }
}
