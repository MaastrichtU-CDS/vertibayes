package com.florian.vertibayes.webservice.domain;

import com.florian.vertibayes.bayes.Node;

import java.util.List;

public class NodesResponse {
    //utility class for passing around Nodes, using full internal class structure, across servers during calculations
    private List<Node> nodes;

    public NodesResponse() {
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
