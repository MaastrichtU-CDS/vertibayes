package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.webservice.domain.external.WebNode;

import java.util.List;

public class CreateNetworkRequest {
    private int minPercentage;
    private List<WebNode> nodes;

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }

    public int getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(int minPercentage) {
        this.minPercentage = minPercentage;
    }
}
