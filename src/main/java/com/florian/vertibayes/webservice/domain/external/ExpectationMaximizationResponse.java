package com.florian.vertibayes.webservice.domain.external;

import java.util.List;

public class ExpectationMaximizationResponse {
    private List<WebNode> nodes;
    private double syntheticTrainingAuc;

    public double getSyntheticTrainingAuc() {
        return syntheticTrainingAuc;
    }

    public void setSyntheticTrainingAuc(double syntheticTrainingAuc) {
        this.syntheticTrainingAuc = syntheticTrainingAuc;
    }

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }

}
