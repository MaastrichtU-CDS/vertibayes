package com.florian.vertibayes.bayes.webservice.mapping;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Network;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.webservice.mapping.WebNodeMapper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebNodeMapperTest {

    @Test
    public void testMapper() {
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

        // map from Node
        List<WebNode> webnodes = WebNodeMapper.mapWebNodeFromNode(nodes);

        assertEquals(webnodes.size(), 3);

        WebNode node1 = webnodes.get(0);
        assertEquals(node1.getName(), "x1");
        assertEquals(node1.getType(), Attribute.AttributeType.numeric);
        assertEquals(node1.getParents().size(), 0);

        WebNode node2 = webnodes.get(1);
        assertEquals(node2.getName(), "x2");
        assertEquals(node2.getType(), Attribute.AttributeType.numeric);
        assertEquals(node2.getParents().size(), 1);
        assertEquals(node2.getParents().get(0), "x1");

        WebNode node3 = webnodes.get(2);
        assertEquals(node3.getName(), "x3");
        assertEquals(node3.getType(), Attribute.AttributeType.string);
        assertEquals(node3.getParents().size(), 1);
        assertEquals(node3.getParents().get(0), "x2");

        // Map to node
        List<Node> mappedNodes = WebNodeMapper.mapWebNodeToNode(webnodes);
        // should result in the same list of nodes as nodes.
        assertEquals(nodes.size(), mappedNodes.size());

        //put mappedNodes in a hashmap so it's easier to find specific nodes to compare:
        Map<String, Node> mappedNodesMap = new HashMap<>();
        for (Node n : mappedNodes) {
            mappedNodesMap.put(n.getName(), n);
        }
        //compare nodes:
        for (Node n : nodes) {
            Node mapped = mappedNodesMap.get(n.getName());
            assertEquals(n.getName(), mapped.getName());
            assertEquals(n.getType(), mapped.getType());
            assertEquals(n.isDiscrete(), mapped.isDiscrete());
            assertEquals(n.getBins(), mapped.getBins());
            assertEquals(n.getProbabilities(), mapped.getProbabilities());

            // put parents in map:
            Map<String, Node> mappedParents = new HashMap<>();
            for (Node p : mapped.getParents()) {
                mappedParents.put(p.getName(), p);
            }
            // compare mappedParents:
            for (Node p : n.getParents()) {
                Node mappedParent = mappedParents.get(p.getName());
                assertEquals(n.getName(), mapped.getName());
                assertEquals(n.getType(), mapped.getType());
            }
        }

    }

    @Test
    public void testMapperMultipleParents() {
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
        //make it so node x3 has 2 parents
        nodes.get(2).getParents().add(nodes.get(0));

        // map from Node
        List<WebNode> webnodes = WebNodeMapper.mapWebNodeFromNode(nodes);

        assertEquals(webnodes.size(), 3);

        WebNode node1 = webnodes.get(0);
        assertEquals(node1.getName(), "x1");
        assertEquals(node1.getType(), Attribute.AttributeType.numeric);
        assertEquals(node1.getParents().size(), 0);

        WebNode node2 = webnodes.get(1);
        assertEquals(node2.getName(), "x2");
        assertEquals(node2.getType(), Attribute.AttributeType.numeric);
        assertEquals(node2.getParents().size(), 1);
        assertEquals(node2.getParents().get(0), "x1");

        WebNode node3 = webnodes.get(2);
        assertEquals(node3.getName(), "x3");
        assertEquals(node3.getType(), Attribute.AttributeType.string);
        assertEquals(node3.getParents().size(), 2);
        assertEquals(node3.getParents().get(0), "x2");

        // Map to node
        List<Node> mappedNodes = WebNodeMapper.mapWebNodeToNode(webnodes);
        // should result in the same list of nodes as nodes.
        assertEquals(nodes.size(), mappedNodes.size());

        //put mappedNodes in a hashmap so it's easier to find specific nodes to compare:
        Map<String, Node> mappedNodesMap = new HashMap<>();
        for (Node n : mappedNodes) {
            mappedNodesMap.put(n.getName(), n);
        }
        //compare nodes:
        for (Node n : nodes) {
            Node mapped = mappedNodesMap.get(n.getName());
            assertEquals(n.getName(), mapped.getName());
            assertEquals(n.getType(), mapped.getType());
            assertEquals(n.isDiscrete(), mapped.isDiscrete());
            assertEquals(n.getBins(), mapped.getBins());
            assertEquals(n.getProbabilities(), mapped.getProbabilities());

            // put parents in map:
            Map<String, Node> mappedParents = new HashMap<>();
            for (Node p : mapped.getParents()) {
                mappedParents.put(p.getName(), p);
            }
            // compare mappedParents:
            for (Node p : n.getParents()) {
                Node mappedParent = mappedParents.get(p.getName());
                assertEquals(n.getName(), mapped.getName());
                assertEquals(n.getType(), mapped.getType());
            }
        }

    }
}