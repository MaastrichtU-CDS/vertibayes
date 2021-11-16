package com.florian.vertibayes.bayes;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class BayesServerTest {

    @Test
    public void InferenceInit() {
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

        Network network = new Network(Arrays.asList(endpoint1, endpoint2), secretEnd, new VertiBayesCentralServer());
        network.createNetwork();
        List<Node> nodes = network.getNodes();

        for (Node n : nodes) {
            // init the thetas
            n.initProbabilities();
            // now give it more manageable values:
            double size = n.getProbabilities().size();
            for (Theta t : n.getProbabilities()) {
                t.setP(1.0 / size);
            }
        }
    }
}