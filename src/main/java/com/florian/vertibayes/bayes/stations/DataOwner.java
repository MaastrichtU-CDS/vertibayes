package com.florian.vertibayes.bayes.stations;

import com.florian.nscalarproduct.station.DataStation;
import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.bayes.data.Data;

import java.math.BigInteger;
import java.util.*;

import static com.florian.vertibayes.bayes.data.Parser.parseCsv;

public class DataOwner extends Server {
    private Data data;
    private Map<String, Set<String>> uniqueValues = new HashMap<>();

    public DataOwner(String id, List<ServerEndpoint> endpoints) {
        this.serverId = id;
        this.setEndpoints(endpoints);
    }

    public DataOwner(String path, String id) {
        this.data = parseCsv(path, 0);
        for (String name : data.getCollumnIds().keySet()) {
            uniqueValues.put(name, Data.getUniqueValues(data.getAttributeValues(name)));
        }
        this.serverId = id;
    }

    public void initData(List<Attribute> requirements) {
        int population = data.getNumberOfIndividuals();
        localData = new BigInteger[population];
        for (int i = 0; i < population; i++) {
            localData[i] = BigInteger.ONE;
        }

        List<List<Attribute>> values = data.getData();
        for (Attribute req : requirements) {
            if (data.getAttributeCollumn(req.getAttributeName()) == null) {
                // attribute not locally available, skip
                continue;
            }
            for (int i = 0; i < population; i++) {
                if (!values.get(data.getAttributeCollumn(req.getAttributeName())).get(i).equals(req)) {
                    localData[i] = BigInteger.ZERO;
                }
            }
        }
        this.population = localData.length;
        this.dataStations.put("start", new DataStation(this.serverId, this.localData));
    }


    public List<Node> createNodes() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < data.getData().size(); i++) {
            if (i == data.getIdColumn()) {
                continue;
            }
            Attribute attribute = data.getData().get(i).get(0);
            nodes.add(new Node(attribute.getAttributeName(), uniqueValues.get(attribute.getAttributeName()),
                               attribute.getType()));
        }
        return nodes;
    }
}
