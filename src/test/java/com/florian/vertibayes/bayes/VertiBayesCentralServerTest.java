package com.florian.vertibayes.bayes;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.MaximumLikelyhoodRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class VertiBayesCentralServerTest {

    @Test
    public void maximumLikelyhood() {
        BayesServer station1 = new BayesServer("resources/smallK2Example_firsthalf.csv", "1");
        BayesServer station2 = new BayesServer("resources/smallK2Example_secondhalf.csv", "2");

        VertiBayesEndpoint endpoint1 = new VertiBayesEndpoint(station1);
        VertiBayesEndpoint endpoint2 = new VertiBayesEndpoint(station2);
        BayesServer secret = new BayesServer("3", Arrays.asList(endpoint1, endpoint2));

        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpoint1);
        all.add(endpoint2);
        all.add(secretEnd);
        secret.setEndpoints(all);
        station1.setEndpoints(all);
        station2.setEndpoints(all);

        VertiBayesCentralServer central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2), secretEnd);
        List<Node> nodes = central.buildNetwork();
        MaximumLikelyhoodRequest req = new MaximumLikelyhoodRequest();
        req.setNodes(nodes);
        central.maximumLikelyhood(req);


        // check if it matches expected network
        assertEquals(nodes.size(), 3);
        assertEquals(nodes.get(0).getParents().size(), 0);
        assertEquals(nodes.get(1).getParents().size(), 1);
        assertTrue(nodes.get(1).getParents().contains(nodes.get(0)));
        assertEquals(nodes.get(2).getParents().size(), 1);
        assertTrue(nodes.get(2).getParents().contains(nodes.get(1)));
        //expected network an example are based on the example in "resources/k2_algorithm.pdf"

        // now check the thetas
        // node 1 has 2 values and no parents, reuslting in 2 values in total
        assertEquals(nodes.get(0).getProbabilities().size(), 2);
        // value 1
        assertEquals(nodes.get(0).getProbabilities().get(0).getP(), 0.5);
        assertEquals(nodes.get(0).getProbabilities().get(0).getLocalValue().getValue(), "0");
        assertEquals(nodes.get(0).getProbabilities().get(0).getLocalValue().getAttributeName(), "x1");
        assertEquals(nodes.get(0).getProbabilities().get(0).getParents().size(), 0);
        // value 2
        assertEquals(nodes.get(0).getProbabilities().get(1).getP(), 0.5);
        assertEquals(nodes.get(0).getProbabilities().get(1).getLocalValue().getValue(), "1");
        assertEquals(nodes.get(0).getProbabilities().get(1).getLocalValue().getAttributeName(), "x1");
        assertEquals(nodes.get(0).getProbabilities().get(1).getParents().size(), 0);

        //node 2 has 2 local values and 1 parent with 2 values resulting in 4 values in total
        // value 1
        assertEquals(nodes.get(1).getProbabilities().size(), 4);
        assertEquals(nodes.get(1).getProbabilities().get(0).getP(), 0.8);
        assertEquals(nodes.get(1).getProbabilities().get(0).getLocalValue().getValue(), "0");
        assertEquals(nodes.get(1).getProbabilities().get(0).getLocalValue().getAttributeName(), "x2");
        assertEquals(nodes.get(1).getProbabilities().get(0).getParents().size(), 1);
        assertEquals(nodes.get(1).getProbabilities().get(0).getParents().get(0).getValue().getValue(), "0");
        assertEquals(nodes.get(1).getProbabilities().get(0).getParents().get(0).getName(), "x1");

        // value 2
        assertEquals(nodes.get(1).getProbabilities().size(), 4);
        assertEquals(nodes.get(1).getProbabilities().get(1).getP(), 0.2);
        assertEquals(nodes.get(1).getProbabilities().get(1).getLocalValue().getValue(), "1");
        assertEquals(nodes.get(1).getProbabilities().get(1).getLocalValue().getAttributeName(), "x2");
        assertEquals(nodes.get(1).getProbabilities().get(1).getParents().size(), 1);
        assertEquals(nodes.get(1).getProbabilities().get(1).getParents().get(0).getValue().getValue(), "0");
        assertEquals(nodes.get(1).getProbabilities().get(1).getParents().get(0).getName(), "x1");

        // value 3
        assertEquals(nodes.get(1).getProbabilities().size(), 4);
        assertEquals(nodes.get(1).getProbabilities().get(2).getP(), 0.2);
        assertEquals(nodes.get(1).getProbabilities().get(2).getLocalValue().getValue(), "0");
        assertEquals(nodes.get(1).getProbabilities().get(2).getLocalValue().getAttributeName(), "x2");
        assertEquals(nodes.get(1).getProbabilities().get(2).getParents().size(), 1);
        assertEquals(nodes.get(1).getProbabilities().get(2).getParents().get(0).getValue().getValue(), "1");
        assertEquals(nodes.get(1).getProbabilities().get(2).getParents().get(0).getName(), "x1");

        // value 4
        assertEquals(nodes.get(1).getProbabilities().size(), 4);
        assertEquals(nodes.get(1).getProbabilities().get(3).getP(), 0.8);
        assertEquals(nodes.get(1).getProbabilities().get(3).getLocalValue().getValue(), "1");
        assertEquals(nodes.get(1).getProbabilities().get(3).getLocalValue().getAttributeName(), "x2");
        assertEquals(nodes.get(1).getProbabilities().get(3).getParents().size(), 1);
        assertEquals(nodes.get(1).getProbabilities().get(3).getParents().get(0).getValue().getValue(), "1");
        assertEquals(nodes.get(1).getProbabilities().get(3).getParents().get(0).getName(), "x1");

        //node 3 has 2 local values and 1 parent with 2 values resulting in 4 values in total
        // value 1
        assertEquals(nodes.get(2).getProbabilities().size(), 4);
        assertEquals(nodes.get(2).getProbabilities().get(0).getP(), 0.8);
        assertEquals(nodes.get(2).getProbabilities().get(0).getLocalValue().getValue(), "0");
        assertEquals(nodes.get(2).getProbabilities().get(0).getLocalValue().getAttributeName(), "x3");
        assertEquals(nodes.get(2).getProbabilities().get(0).getParents().size(), 1);
        assertEquals(nodes.get(2).getProbabilities().get(0).getParents().get(0).getValue().getValue(), "0");
        assertEquals(nodes.get(2).getProbabilities().get(0).getParents().get(0).getName(), "x2");

        // value 2
        assertEquals(nodes.get(2).getProbabilities().size(), 4);
        assertEquals(nodes.get(2).getProbabilities().get(1).getP(), 0.2);
        assertEquals(nodes.get(2).getProbabilities().get(1).getLocalValue().getValue(), "1");
        assertEquals(nodes.get(2).getProbabilities().get(1).getLocalValue().getAttributeName(), "x3");
        assertEquals(nodes.get(2).getProbabilities().get(1).getParents().size(), 1);
        assertEquals(nodes.get(2).getProbabilities().get(1).getParents().get(0).getValue().getValue(), "0");
        assertEquals(nodes.get(2).getProbabilities().get(1).getParents().get(0).getName(), "x2");

        // value 3
        assertEquals(nodes.get(2).getProbabilities().size(), 4);
        assertEquals(nodes.get(2).getProbabilities().get(2).getP(), 0.0);
        assertEquals(nodes.get(2).getProbabilities().get(2).getLocalValue().getValue(), "0");
        assertEquals(nodes.get(2).getProbabilities().get(2).getLocalValue().getAttributeName(), "x3");
        assertEquals(nodes.get(2).getProbabilities().get(2).getParents().size(), 1);
        assertEquals(nodes.get(2).getProbabilities().get(2).getParents().get(0).getValue().getValue(), "1");
        assertEquals(nodes.get(2).getProbabilities().get(2).getParents().get(0).getName(), "x2");

        // value 4
        assertEquals(nodes.get(2).getProbabilities().size(), 4);
        assertEquals(nodes.get(2).getProbabilities().get(3).getP(), 1);
        assertEquals(nodes.get(2).getProbabilities().get(3).getLocalValue().getValue(), "1");
        assertEquals(nodes.get(2).getProbabilities().get(3).getLocalValue().getAttributeName(), "x3");
        assertEquals(nodes.get(2).getProbabilities().get(3).getParents().size(), 1);
        assertEquals(nodes.get(2).getProbabilities().get(3).getParents().get(0).getValue().getValue(), "1");
        assertEquals(nodes.get(2).getProbabilities().get(3).getParents().get(0).getName(), "x2");
    }
}