package com.florian.vertibayes.webservice.domain.external;

import java.util.List;

public class ExpectationMaximizationResponse {
    private List<WebNode> nodes;

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }

}
