package com.florian.vertibayes.webservice;

import com.florian.nscalarproduct.station.DataStation;
import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.bayes.data.Data;
import com.florian.vertibayes.webservice.domain.AttributeRequirementsRequest;
import com.florian.vertibayes.webservice.domain.NodesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.*;

import static com.florian.vertibayes.bayes.data.Parser.parseCsv;

@RestController
public class BayesServer extends Server {
    private Data data;
    private Map<String, Set<String>> uniqueValues = new HashMap<>();

    @Value ("${datapath}")
    private String path;

    public BayesServer() {
        super();
    }

    public BayesServer(String id, List<ServerEndpoint> endpoints) {
        this.serverId = id;
        this.setEndpoints(endpoints);
    }

    public BayesServer(String path, String id) {
        this.path = path;
        this.serverId = id;
        readData();
    }

    private void readData() {
        this.data = parseCsv(path, 0);
        for (String name : data.getCollumnIds().keySet()) {
            uniqueValues.put(name, Data.getUniqueValues(data.getAttributeValues(name)));
        }
    }

    @PutMapping ("initK2Data")
    public void initK2Data(@RequestBody AttributeRequirementsRequest request) {
        reset();
        readData();
        List<Attribute> requirements = request.getRequirements() == null ? new ArrayList<>() :
                request.getRequirements();
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

    @GetMapping ("createNodes")
    public NodesResponse createNodes() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < data.getData().size(); i++) {
            if (i == data.getIdColumn()) {
                continue;
            }
            Attribute attribute = data.getData().get(i).get(0);
            nodes.add(new Node(attribute.getAttributeName(), uniqueValues.get(attribute.getAttributeName()),
                               attribute.getType()));
        }
        NodesResponse response = new NodesResponse();
        response.setNodes(nodes);
        return response;
    }

    @Override
    protected void reset() {
        dataStations = new HashMap<>();
        secretStations = new HashMap<>();
    }
}