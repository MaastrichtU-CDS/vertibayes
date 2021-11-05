package com.florian.vertibayes.bayes.data;

import java.util.ArrayList;
import java.util.List;


public class Attribute implements Comparable<Attribute> {
    public enum AttributeType { bool, string, number }

    private AttributeType type;
    private String value;
    private String attributeName;
    private String id;

    public Attribute() {
    }

    public Attribute(AttributeType type, String value, String attributeName) {
        this.type = type;
        this.value = value;
        this.attributeName = attributeName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getValue() {
        return value;
    }

    public AttributeType getType() {
        return type;
    }

    @Override
    public int compareTo(Attribute attribute) {
        if (type != attribute.type) {
            //error
            System.out.println("Checkstyle wants there to be stuff");
            //ToDo implement error handling
        }

        if (type == AttributeType.bool) {
            return Boolean.parseBoolean(value) == Boolean.parseBoolean(attribute.getValue()) ? 0 : 1;
        } else if (type == AttributeType.string) {
            return value.equals(attribute.getValue()) ? 0 : 1;
        } else if (type == AttributeType.number) {
            return Double.compare(Double.parseDouble(value), Double.parseDouble(attribute.getValue()));
        }
        //should never come here, but java wants it
        return 0;
    }

    public int compareTo(String value) {
        if (type == AttributeType.bool) {
            return Boolean.parseBoolean(this.value) == Boolean.parseBoolean(value) ? 0 : 1;
        } else if (type == AttributeType.string) {
            return this.value.equals(value) ? 0 : 1;
        } else if (type == AttributeType.number) {
            return Double.compare(Double.parseDouble(this.value), Double.parseDouble(value));
        }
        //should never come here, but java wants it
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Attribute)) {
            return false;
        }
        Attribute attribute = (Attribute) o;
        return type == attribute.type && compareTo(attribute) == 0 && attributeName.equals(attribute.attributeName);
    }


    @Override
    public int hashCode() {
        int hash = 1;
        final int prime = 5;

        hash = prime * hash + (type == null ? 0 : type.hashCode());
        hash = prime * hash + (value == null ? 0 : value.hashCode());
        hash = prime * hash + (attributeName == null ? 0 : attributeName.hashCode());
        hash = prime * hash + (id == null ? 0 : id.hashCode());
        return hash;
    }

    public static List<Attribute> unique(List<Attribute> attributes) {
        List<Attribute> unique = new ArrayList<>();
        for (Attribute attribute : attributes) {
            if (!unique.contains(attribute)) {
                unique.add(attribute);
            }
        }
        return unique;
    }
}
