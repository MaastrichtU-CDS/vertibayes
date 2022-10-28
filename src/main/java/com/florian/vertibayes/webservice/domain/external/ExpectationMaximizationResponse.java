package com.florian.vertibayes.webservice.domain.external;

import java.util.List;

public class ExpectationMaximizationResponse {
    private List<WebNode> nodes;
    private double scvAuc;
    private double svdgAuc;

    public double getSvdgAuc() {
        return svdgAuc;
    }

    public void setSvdgAuc(double svdgAuc) {
        this.svdgAuc = svdgAuc;
    }

    public double getScvAuc() {
        return scvAuc;
    }

    public void setScvAuc(double scvAuc) {
        this.scvAuc = scvAuc;
    }

    public List<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WebNode> nodes) {
        this.nodes = nodes;
    }

}
