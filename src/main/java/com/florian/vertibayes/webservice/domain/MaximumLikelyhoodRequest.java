package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.bayes.Node;

import java.util.List;

public class MaximumLikelyhoodRequest {
    private List<Node> nodes;

    public MaximumLikelyhoodRequest() {
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
