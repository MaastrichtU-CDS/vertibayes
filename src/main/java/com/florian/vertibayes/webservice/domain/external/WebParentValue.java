package com.florian.vertibayes.webservice.domain.external;

public class WebParentValue {
    private String parent;
    private WebValue value;

    public WebParentValue() {
    }

    public WebParentValue(String parent, WebValue value) {
        this.parent = parent;
        this.value = value;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public WebValue getValue() {
        return value;
    }

    public void setValue(WebValue value) {
        this.value = value;
    }
}
