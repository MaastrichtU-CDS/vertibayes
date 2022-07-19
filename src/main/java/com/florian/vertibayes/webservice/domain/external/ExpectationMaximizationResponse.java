package com.florian.vertibayes.webservice.domain.external;

import java.util.List;

public class ExpectationMaximizationResponse {
    private List<WebNode> nodes;
    private double auc;

    public double getAuc() {
        return auc;
    }

    public void setAuc(double auc) {
        this.auc = auc;
    }

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }

}
