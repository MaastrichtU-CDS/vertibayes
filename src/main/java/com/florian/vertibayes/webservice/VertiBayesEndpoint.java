package com.florian.vertibayes.webservice;


import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirementsRequest;
import com.florian.vertibayes.webservice.domain.NodesResponse;

import java.util.List;
import java.util.Set;

public class VertiBayesEndpoint extends ServerEndpoint {

    public VertiBayesEndpoint(Server server) {
        super(server);
    }

    public VertiBayesEndpoint(String url) {
        super(url);
    }

    public void initK2Data(List<Attribute> req) {
        AttributeRequirementsRequest request = new AttributeRequirementsRequest();
        request.setRequirements(req);
        if (testing) {
            ((BayesServer) (server)).initK2Data(request);
        } else {
            REST_TEMPLATE.put(serverUrl + "/initK2Data", request);
        }
    }

    public List<Node> createNode() {
        if (testing) {
            return ((BayesServer) (server)).createNodes().getNodes();
        }
        return REST_TEMPLATE.getForEntity(serverUrl + "/createNodes", NodesResponse.class).getBody().getNodes();
    }

    public Set<String> getUniqueValues(String attributeName) {
        if (testing) {
            return ((BayesServer) (server)).getUniqueValues(attributeName);
        }
        return REST_TEMPLATE.getForEntity(serverUrl + "/getUniqueValues?attribute=" + attributeName, Set.class)
                .getBody();
    }
}
