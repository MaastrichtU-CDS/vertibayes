package com.florian.vertibayes.bayes;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NetworkTest {

    @Test
    public void testCreateNetwork() {
        BayesServer station1 = new BayesServer("resources/Experiments/k2/smallK2Example_firsthalf.csv", "1");
        BayesServer station2 = new BayesServer("resources/Experiments/k2/smallK2Example_secondhalf.csv", "2");

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

        // check if it matches expected network
        assertEquals(nodes.size(), 3);
        assertEquals(nodes.get(0).getParents().size(), 0);
        assertEquals(nodes.get(1).getParents().size(), 1);
        assertTrue(nodes.get(1).getParents().contains(nodes.get(0)));
        assertEquals(nodes.get(2).getParents().size(), 1);
        assertTrue(nodes.get(2).getParents().contains(nodes.get(1)));

        //expected network an example are based on the example in "resources/k2_algorithm.pdf"
    }

    @Test
    public void testDetermineRequirements() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("1", new HashSet<String>() {{
            add("a");
            add("b");
        }}, Attribute.AttributeType.string));
        nodes.add(new Node("2", new HashSet<String>() {{
            add("c");
            add("d");
        }}, Attribute.AttributeType.string));
        nodes.add(new Node("3", new HashSet<String>() {{
            add("e");
            add("f");
        }}, Attribute.AttributeType.string));

        Network net = new Network(new ArrayList<>(), null, null);
        List<List<Attribute>> requirements = net.determineRequirements(nodes);
        List<List<Attribute>> expected = new ArrayList<>();
        List<Attribute> exp = new ArrayList<>();
        exp.add(new Attribute(Attribute.AttributeType.string, "a", "1"));
        exp.add(new Attribute(Attribute.AttributeType.string, "c", "2"));
        exp.add(new Attribute(Attribute.AttributeType.string, "e", "3"));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new Attribute(Attribute.AttributeType.string, "a", "1"));
        exp.add(new Attribute(Attribute.AttributeType.string, "c", "2"));
        exp.add(new Attribute(Attribute.AttributeType.string, "f", "3"));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new Attribute(Attribute.AttributeType.string, "a", "1"));
        exp.add(new Attribute(Attribute.AttributeType.string, "d", "2"));
        exp.add(new Attribute(Attribute.AttributeType.string, "e", "3"));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new Attribute(Attribute.AttributeType.string, "a", "1"));
        exp.add(new Attribute(Attribute.AttributeType.string, "d", "2"));
        exp.add(new Attribute(Attribute.AttributeType.string, "f", "3"));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new Attribute(Attribute.AttributeType.string, "b", "1"));
        exp.add(new Attribute(Attribute.AttributeType.string, "c", "2"));
        exp.add(new Attribute(Attribute.AttributeType.string, "e", "3"));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new Attribute(Attribute.AttributeType.string, "b", "1"));
        exp.add(new Attribute(Attribute.AttributeType.string, "c", "2"));
        exp.add(new Attribute(Attribute.AttributeType.string, "f", "3"));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new Attribute(Attribute.AttributeType.string, "b", "1"));
        exp.add(new Attribute(Attribute.AttributeType.string, "d", "2"));
        exp.add(new Attribute(Attribute.AttributeType.string, "e", "3"));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new Attribute(Attribute.AttributeType.string, "b", "1"));
        exp.add(new Attribute(Attribute.AttributeType.string, "d", "2"));
        exp.add(new Attribute(Attribute.AttributeType.string, "f", "3"));
        expected.add(exp);

        assertEquals(requirements.size(), expected.size());
        for (List<Attribute> ex : expected) {
            assertTrue(requirements.contains(exp));
        }
    }
}