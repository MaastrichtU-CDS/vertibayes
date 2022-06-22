package com.florian.vertibayes.bayes;

public class Bin {
    private String upperLimit;
    private String lowerLimit;

    public String getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(String upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(String lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else {
            return ((Bin) o).getUpperLimit().equals(upperLimit) && ((Bin) o).getLowerLimit().equals(lowerLimit);
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        final int prime = 5;

        hash = prime * hash + (upperLimit == null ? 0 : upperLimit.hashCode());
        hash = prime * hash + (lowerLimit == null ? 0 : lowerLimit.hashCode());
        return hash;
    }
}
