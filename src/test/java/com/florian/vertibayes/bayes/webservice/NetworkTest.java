package com.florian.vertibayes.bayes.webservice;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Network;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.CreateNetworkRequest;
import com.florian.vertibayes.webservice.domain.external.WebNode;
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

        Network network = new Network(Arrays.asList(endpoint1, endpoint2), secretEnd, new VertiBayesCentralServer(),
                                      100);
        CreateNetworkRequest req = new CreateNetworkRequest();
        req.setMinPercentage(10);
        req.setNodes(createK2Nodes());
        network.createNetwork(req);
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
    public void testCreateNetworkMissing() {
        BayesServer station1 = new BayesServer("resources/Experiments/k2/missingString_First_Half.csv", "1");
        BayesServer station2 = new BayesServer("resources/Experiments/k2/missingString_Second_Half.csv", "2");

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

        Network network = new Network(Arrays.asList(endpoint1, endpoint2), secretEnd, new VertiBayesCentralServer(),
                                      100);
        CreateNetworkRequest req = new CreateNetworkRequest();
        req.setMinPercentage(10);
        network.createNetwork(req);
        List<Node> nodes = network.getNodes();

        // check if it matches expected network
        // This still results in a slightly different model than without missing values
        assertEquals(nodes.size(), 3);
        assertEquals(nodes.get(0).getParents().size(), 0);
        assertEquals(nodes.get(1).getParents().size(), 1);
        assertTrue(nodes.get(1).getParents().contains(nodes.get(0)));
        assertEquals(nodes.get(2).getParents().size(), 1);
        assertTrue(nodes.get(2).getParents().contains(nodes.get(0)));

        //expected network an example are based on the example in "resources/k2_algorithm.pdf"
    }

    @Test
    public void testCreateNetworkHybrid() {
        BayesServer station1 = new BayesServer("resources/Experiments/hybridsplit/smallK2Example_firsthalf_hybrid1.csv",
                                               "1");
        BayesServer station2 = new BayesServer("resources/Experiments/hybridsplit/smallK2Example_firsthalf_hybrid2.csv",
                                               "2");
        BayesServer station3 = new BayesServer("resources/Experiments/hybridsplit/smallK2Example_secondhalf.csv", "3");

        VertiBayesEndpoint endpoint1 = new VertiBayesEndpoint(station1);
        VertiBayesEndpoint endpoint2 = new VertiBayesEndpoint(station2);
        VertiBayesEndpoint endpoint3 = new VertiBayesEndpoint(station3);
        BayesServer secret = new BayesServer("4", Arrays.asList(endpoint1, endpoint2, endpoint3));

        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpoint1);
        all.add(endpoint2);
        all.add(endpoint3);
        all.add(secretEnd);
        secret.setEndpoints(all);
        station1.setEndpoints(all);
        station2.setEndpoints(all);
        station3.setEndpoints(all);

        Network network = new Network(Arrays.asList(endpoint1, endpoint3), secretEnd,
                                      new VertiBayesCentralServer(),
                                      100);
        endpoint1.setUseLocalOnly(true);
        CreateNetworkRequest req = new CreateNetworkRequest();
        req.setMinPercentage(10);
        req.setNodes(createK2Nodes());
        network.createNetwork(req);

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
    public void testCreateThreePartyNetwork() {
        BayesServer station1 = new BayesServer("resources/Experiments/threeParty/Asia10k_first.csv", "1");
        BayesServer station2 = new BayesServer("resources/Experiments/threeParty/Asia10k_second.csv", "2");
        BayesServer station3 = new BayesServer("resources/Experiments/threeParty/Asia10k_third.csv", "3");

        VertiBayesEndpoint endpoint1 = new VertiBayesEndpoint(station1);
        VertiBayesEndpoint endpoint2 = new VertiBayesEndpoint(station2);
        VertiBayesEndpoint endpoint3 = new VertiBayesEndpoint(station3);
        BayesServer secret = new BayesServer("4", Arrays.asList(endpoint1, endpoint2, endpoint3));

        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpoint1);
        all.add(endpoint2);
        all.add(endpoint3);
        all.add(secretEnd);
        secret.setEndpoints(all);
        station1.setEndpoints(all);
        station2.setEndpoints(all);
        station3.setEndpoints(all);

        Network network = new Network(Arrays.asList(endpoint3, endpoint2, endpoint1), secretEnd,
                                      new VertiBayesCentralServer(), 10000);
        CreateNetworkRequest req = new CreateNetworkRequest();
        req.setMinPercentage(10);
        network.createNetwork(req);
        List<Node> nodes = network.getNodes();

        // check if it matches expected network
        assertEquals(nodes.size(), 8);
        assertEquals(nodes.get(0).getName(), "lung");
        assertEquals(nodes.get(0).getParents().size(), 0);

        assertEquals(nodes.get(1).getName(), "bronc");
        assertEquals(nodes.get(1).getParents().size(), 1);
        assertEquals(nodes.get(1).getParents().get(0).getName(), "lung");

        assertEquals(nodes.get(2).getName(), "either");
        assertEquals(nodes.get(2).getParents().size(), 1);
        assertEquals(nodes.get(2).getParents().get(0).getName(), "lung");

        assertEquals(nodes.get(3).getName(), "xray");
        assertEquals(nodes.get(3).getParents().size(), 1);
        assertEquals(nodes.get(3).getParents().get(0).getName(), "either");

        assertEquals(nodes.get(4).getName(), "dysp");
        assertEquals(nodes.get(4).getParents().size(), 1);
        assertEquals(nodes.get(4).getParents().get(0).getName(), "bronc");

        assertEquals(nodes.get(5).getName(), "asia");
        assertEquals(nodes.get(5).getParents().size(), 1);
        assertEquals(nodes.get(5).getParents().get(0).getName(), "either");

        assertEquals(nodes.get(6).getName(), "tub");
        assertEquals(nodes.get(6).getParents().size(), 1);
        assertEquals(nodes.get(6).getParents().get(0).getName(), "either");

        assertEquals(nodes.get(7).getName(), "smoke");
        assertEquals(nodes.get(7).getParents().size(), 1);
        assertEquals(nodes.get(7).getParents().get(0).getName(), "bronc");
    }

    @Test
    public void testCreateDifferentFileTypeNetwork() {
        BayesServer station1 = new BayesServer("resources/Experiments/mixedFiles/allTypes.csv", "1");
        BayesServer station2 = new BayesServer("resources/Experiments/mixedFiles/allTypes.arff", "2");
        BayesServer station3 = new BayesServer("resources/Experiments/mixedFiles/allTypes.parquet", "3");

        VertiBayesEndpoint endpoint1 = new VertiBayesEndpoint(station1);
        VertiBayesEndpoint endpoint2 = new VertiBayesEndpoint(station2);
        VertiBayesEndpoint endpoint3 = new VertiBayesEndpoint(station3);
        BayesServer secret = new BayesServer("4", Arrays.asList(endpoint1, endpoint2, endpoint3));

        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpoint1);
        all.add(endpoint2);
        all.add(endpoint3);
        all.add(secretEnd);
        secret.setEndpoints(all);
        station1.setEndpoints(all);
        station2.setEndpoints(all);
        station3.setEndpoints(all);

        Network network = new Network(Arrays.asList(endpoint3, endpoint2, endpoint1), secretEnd,
                                      new VertiBayesCentralServer(), 10000);
        CreateNetworkRequest req = new CreateNetworkRequest();
        req.setMinPercentage(10);
        network.createNetwork(req);
        List<Node> nodes = network.getNodes();

        // check if it matches expected network
        assertEquals(nodes.size(), 12);
        assertEquals(nodes.get(0).getName(), "numeric_parquet");
        assertEquals(nodes.get(0).getType(), Attribute.AttributeType.numeric);

        assertEquals(nodes.get(1).getName(), "real_parquet");
        assertEquals(nodes.get(1).getType(), Attribute.AttributeType.real);

        assertEquals(nodes.get(2).getName(), "string_parquet");
        assertEquals(nodes.get(2).getType(), Attribute.AttributeType.string);

        assertEquals(nodes.get(3).getName(), "bool_parquet");
        assertEquals(nodes.get(3).getType(), Attribute.AttributeType.bool);

        assertEquals(nodes.get(4).getName(), "real_arff");
        assertEquals(nodes.get(4).getType(), Attribute.AttributeType.real);

        assertEquals(nodes.get(5).getName(), "numeric_arff");
        assertEquals(nodes.get(5).getType(), Attribute.AttributeType.numeric);

        assertEquals(nodes.get(6).getName(), "string_arff");
        assertEquals(nodes.get(6).getType(), Attribute.AttributeType.string);

        assertEquals(nodes.get(7).getName(), "bool_arff");
        assertEquals(nodes.get(7).getType(), Attribute.AttributeType.bool);

        assertEquals(nodes.get(8).getName(), "numeric_csv");
        assertEquals(nodes.get(8).getType(), Attribute.AttributeType.numeric);

        assertEquals(nodes.get(9).getName(), "real_csv");
        assertEquals(nodes.get(9).getType(), Attribute.AttributeType.real);

        assertEquals(nodes.get(10).getName(), "string_csv");
        assertEquals(nodes.get(10).getType(), Attribute.AttributeType.string);

        assertEquals(nodes.get(11).getName(), "bool_csv");
        assertEquals(nodes.get(11).getType(), Attribute.AttributeType.bool);
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

        Network net = new Network(new ArrayList<>(), null, null, 100);
        List<List<AttributeRequirement>> requirements = net.determineRequirements(nodes);
        List<List<AttributeRequirement>> expected = new ArrayList<>();
        List<AttributeRequirement> exp = new ArrayList<>();
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "a", "1")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "c", "2")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "e", "3")));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "a", "1")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "c", "2")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "f", "3")));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "a", "1")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "d", "2")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "e", "3")));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "a", "1")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "d", "2")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "f", "3")));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "b", "1")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "c", "2")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "e", "3")));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "b", "1")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "c", "2")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "f", "3")));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "b", "1")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "d", "2")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "e", "3")));
        expected.add(exp);
        exp = new ArrayList<>();
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "b", "1")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "d", "2")));
        exp.add(new AttributeRequirement(new Attribute(Attribute.AttributeType.string, "f", "3")));
        expected.add(exp);

        assertEquals(requirements.size(), expected.size());
        expected.get(0).get(0).equals(requirements.get(0).get(0));
        for (List<AttributeRequirement> ex : expected) {
            assertTrue(requirements.contains(ex));
        }
    }

    public static List<WebNode> createK2Nodes() {
        List<WebNode> nodes = new ArrayList<>();
        WebNode x1 = new WebNode();
        x1.setType(Attribute.AttributeType.numeric);
        x1.setName("x1");
        WebNode x2 = new WebNode();
        x2.setType(Attribute.AttributeType.numeric);
        x2.setName("x2");
        WebNode x3 = new WebNode();
        x3.setType(Attribute.AttributeType.string);
        x3.setName("x3");

        nodes.add(x1);
        nodes.add(x2);
        nodes.add(x3);

        Bin one = new Bin();
        one.setUpperLimit("1.5");
        one.setLowerLimit("0.5");

        Bin zero = new Bin();
        zero.setUpperLimit("0.5");
        zero.setLowerLimit("-0.5");

        for (WebNode node : nodes) {
            if (!node.getName().equals("x3")) {
                node.getBins().add(zero);
                node.getBins().add(one);
            }
        }

        return nodes;

    }
}