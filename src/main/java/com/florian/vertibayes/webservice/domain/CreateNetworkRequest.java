package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.webservice.domain.external.WebNode;

import java.util.List;

public class CreateNetworkRequest {
    private double minPercentage;
    private List<WebNode> nodes;

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(double minPercentage) {
        this.minPercentage = minPercentage;
    }
}
