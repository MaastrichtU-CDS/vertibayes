package com.florian.vertibayes.webservice.domain.external;

import java.util.List;

public class WebBayesNetwork {
    // utility class for communicating about BayesNetworks with the outside world
    private List<WebNode> nodes;
    private String target;
    private double minPercentage;
    private boolean openMarkovResponse = false;
    private boolean wekaResponse = false;
    private int folds = 1;

    private static final int MIN_FOLDS = 1;
    private static final int MAX_FOLDS = 10;

    public int getFolds() {
        return folds;
    }

    public void setFolds(int folds) {
        if (folds > MAX_FOLDS) {
            // max folds = 10
            this.folds = MAX_FOLDS;
        } else if (folds < MIN_FOLDS) {
            this.folds = MIN_FOLDS;
        } else {
            this.folds = folds;
        }
    }

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

    public boolean isWekaResponse() {
        return wekaResponse;
    }

    public void setWekaResponse(boolean wekaResponse) {
        this.wekaResponse = wekaResponse;
    }
}
