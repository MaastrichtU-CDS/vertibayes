package com.florian.vertibayes.bayes.data;

import java.util.*;

public class Data {

    private int idColumn;
    private List<List<Attribute>> data;
    private HashMap<String, Integer> collumnIds;


    public Data(int idColumn, List<List<Attribute>> data) {
        this.data = data;
        initCollumnIds();
        this.idColumn = idColumn;
        initIds();

    }

    public HashMap<String, Integer> getCollumnIds() {
        return collumnIds;
    }


    public int getNumberOfIndividuals() {
        return getIds().size();
    }

    public int getIdColumn() {
        return idColumn;
    }

    public List<Attribute> getIds() {
        return data.get(idColumn);
    }

    public List<List<Attribute>> getData() {
        return data;
    }

    public Integer getIndividualRow(String id) {
        //Returns the first row it finds containing this ID
        //If no idea is found return -1
        //Assumption is that IDs are unique
        List<Attribute> ids = getIds();
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i).getValue().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public List<Attribute> getAttributeValues(String name) {
        return collumnIds.get(name) == null ? new ArrayList<Attribute>() : data.get(collumnIds.get(name));
    }

    public Integer getAttributeCollumn(String name) {
        return collumnIds.get(name);
    }

    private void initCollumnIds() {
        collumnIds = new HashMap<>();
        int collumn = 0;
        for (List<Attribute> attribute : data) {
            collumnIds.put(attribute.get(0).getAttributeName(), collumn);
            collumn++;
        }
    }

    private void initIds() {
        for (List<Attribute> attributes : data) {
            for (int i = 0; i < attributes.size(); i++) {
                attributes.get(i).setId(data.get(getIdColumn()).get(i).getValue());
            }
        }
    }

    public static Set<String> getUniqueValues(List<Attribute> attributeValues) {
        Set<String> unique = new HashSet<>();
        for (Attribute att : attributeValues) {
            unique.add(att.getValue());
        }
        return unique;
    }
}
