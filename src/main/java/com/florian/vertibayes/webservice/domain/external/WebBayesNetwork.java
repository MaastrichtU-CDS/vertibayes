package com.florian.vertibayes.webservice.domain.external;

import java.util.List;

public class WebBayesNetwork {
    // utility class for communicating about BayesNetworks with the outside world
    private List<WebNode> nodes;

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }
}
