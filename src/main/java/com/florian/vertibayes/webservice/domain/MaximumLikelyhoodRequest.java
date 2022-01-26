package com.florian.vertibayes.webservice.domain;

import java.util.List;

public class MaximumLikelyhoodRequest {
    private List<WebNode> nodes;

    public MaximumLikelyhoodRequest() {
    }

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }
}
