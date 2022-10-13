package com.florian.vertibayes.webservice.domain.external;

import java.util.List;

public class WebBayesNetwork {
    // utility class for communicating about BayesNetworks with the outside world
    private List<WebNode> nodes;
    private String target;
    private double minPercentage;
    private boolean openMarkovResponse = false;

    public boolean isOpenMarkovResponse() {
        return openMarkovResponse;
    }

    public void setOpenMarkovResponse(boolean openMarkovResponse) {
        this.openMarkovResponse = openMarkovResponse;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(double minPercentage) {
        this.minPercentage = minPercentage;
    }

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
