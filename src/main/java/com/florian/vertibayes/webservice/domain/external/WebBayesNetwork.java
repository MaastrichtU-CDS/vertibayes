package com.florian.vertibayes.webservice.domain.external;

import java.util.List;

public class WebBayesNetwork {
    // utility class for communicating about BayesNetworks with the outside world
    private List<WebNode> nodes;
    private String target;

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
