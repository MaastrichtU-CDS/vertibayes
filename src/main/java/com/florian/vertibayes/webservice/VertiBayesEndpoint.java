package com.florian.vertibayes.webservice;


import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirementsRequest;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.webservice.domain.ActiveRecordRequest;
import com.florian.vertibayes.webservice.domain.InitDataResponse;
import com.florian.vertibayes.webservice.domain.NodesResponse;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public class VertiBayesEndpoint extends ServerEndpoint {

    public VertiBayesEndpoint(Server server) {
        super(server);
    }

    public VertiBayesEndpoint(String url) {
        super(url);
    }

    public InitDataResponse initK2Data(List<AttributeRequirement> req) {
        AttributeRequirementsRequest request = new AttributeRequirementsRequest();
        request.setRequirements(req);
        if (testing) {
            return ((BayesServer) (server)).initK2Data(request);
        } else {
            return REST_TEMPLATE.postForEntity(serverUrl + "/initK2Data", request, InitDataResponse.class).getBody();
        }
    }

    public InitDataResponse initMaximumLikelyhoodData(List<AttributeRequirement> req) {
        AttributeRequirementsRequest request = new AttributeRequirementsRequest();
        request.setRequirements(req);
        if (testing) {
            return ((BayesServer) (server)).initMaximumLikelyhoodData(request);
        } else {
            return REST_TEMPLATE.postForEntity(serverUrl + "/initMaximumLikelyhoodData", request,
                                               InitDataResponse.class).getBody();
        }
    }

    public void setActiveRecords(boolean[] activeRecords) {
        ActiveRecordRequest request = new ActiveRecordRequest();
        request.setActiveRecords(activeRecords);
        if (testing) {
            ((BayesServer) (server)).setActiveRecords(request);
        } else {
            REST_TEMPLATE.postForEntity(serverUrl + "/setActiveRecords", request, void.class);
        }
    }

    public List<Node> createNode() {
        if (testing) {
            return ((BayesServer) (server)).createNodes().getNodes();
        }
        return REST_TEMPLATE.getForEntity(serverUrl + "/createNodes", NodesResponse.class).getBody().getNodes();
    }

    public void setUseLocalOnly(boolean useLocalOnly) {
        if (testing) {
            ((BayesServer) (server)).setUseLocalOnly(useLocalOnly);
        } else {
            REST_TEMPLATE.getForEntity(serverUrl + "/setUseLocalOnly?localonly=" + useLocalOnly, Set.class)
                    .getBody();
        }
    }

    public Integer getLocalPopulation() {
        if (testing) {
            return ((BayesServer) (server)).getLocalPopulation();
        } else {
            return REST_TEMPLATE.getForEntity(serverUrl + "/getLocalPopulation", Integer.class)
                    .getBody();
        }
    }

    public Set<String> getUniqueValues(String attributeName) {
        if (testing) {
            return ((BayesServer) (server)).getUniqueValues(attributeName);
        }
        return REST_TEMPLATE.getForEntity(serverUrl + "/getUniqueValues?attribute=" + attributeName, Set.class)
                .getBody();
    }

    public Set<Bin> getBins(String attributeName, double minPercentage) {
        if (testing) {
            return ((BayesServer) (server)).getBins(attributeName, minPercentage);
        }
        return REST_TEMPLATE.getForEntity(serverUrl + "/getBins?attribute=" + attributeName
                                                  + "&minPercentage=" + minPercentage, Set.class)
                .getBody();
    }

    public BigInteger getCount() {
        if (testing) {
            return ((BayesServer) (server)).getCount();
        }
        return REST_TEMPLATE.getForEntity(serverUrl + "/getCount", BigInteger.class)
                .getBody();
    }
}
