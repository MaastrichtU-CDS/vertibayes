package com.florian.vertibayes.webservice;


import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.bayes.stations.DataOwner;

import java.util.List;

public class VertiBayesEndpoint extends ServerEndpoint {

    public VertiBayesEndpoint(Server server) {
        super(server);
    }

    public VertiBayesEndpoint(String url) {
        super(url);
    }

    public void initData(List<Attribute> req) {
        ((DataOwner) (server)).initData(req);
    }

    public List<Node> createNode() {
        return ((DataOwner) (server)).createNodes();
    }
}
