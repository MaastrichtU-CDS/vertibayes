package com.florian.vertibayes.bayes.webservice;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.MaximumLikelyhoodRequest;
import com.florian.vertibayes.webservice.domain.WebNode;
import com.florian.vertibayes.webservice.mapping.WebNodeMapper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class VertiBayesCentralServerTest {

    @Test
    public void testMaximumLikelyhood() {
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

        VertiBayesCentralServer central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2), secretEnd);
        List<Node> nodes = central.buildNetwork();
        MaximumLikelyhoodRequest req = new MaximumLikelyhoodRequest();
        req.setNodes(WebNodeMapper.mapWebNodeFromNode(nodes));

        nodes = central.maximumLikelyhood(req);

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

        //assert all probabilities are viewed as sliblings as there are no parents
        List<Theta> sliblings = Node.findSliblings(nodes.get(0).getProbabilities().get(0), nodes.get(0));
        assertTrue(nodes.get(0).getProbabilities().containsAll(sliblings));
        assertTrue(sliblings.containsAll(nodes.get(0).getProbabilities()));

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

        //assert there are two sets of sliblings as there is 1 parent with 2 unique values
        //Set 1=2 and set 3=4 are supposed to be equal due to the starting node used
        List<Theta> sliblings1 = Node.findSliblings(nodes.get(1).getProbabilities().get(0), nodes.get(1));
        List<Theta> sliblings2 = Node.findSliblings(nodes.get(1).getProbabilities().get(1), nodes.get(1));
        List<Theta> sliblings3 = Node.findSliblings(nodes.get(1).getProbabilities().get(2), nodes.get(1));
        List<Theta> sliblings4 = Node.findSliblings(nodes.get(1).getProbabilities().get(3), nodes.get(1));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertFalse(sliblings2.containsAll(sliblings3));
        assertTrue(sliblings3.containsAll(sliblings4));
        assertTrue(sliblings4.containsAll(sliblings3));
        sliblings1.addAll(sliblings3);
        assertTrue(sliblings1.containsAll(nodes.get(1).getProbabilities()));

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
        // test data would expect 0.0 here, but minimum likelyhood forces it to 0.001
        assertEquals(nodes.get(2).getProbabilities().get(2).getP(), 0.001);
        assertEquals(nodes.get(2).getProbabilities().get(2).getLocalValue().getValue(), "0");
        assertEquals(nodes.get(2).getProbabilities().get(2).getLocalValue().getAttributeName(), "x3");
        assertEquals(nodes.get(2).getProbabilities().get(2).getParents().size(), 1);
        assertEquals(nodes.get(2).getProbabilities().get(2).getParents().get(0).getValue().getValue(), "1");
        assertEquals(nodes.get(2).getProbabilities().get(2).getParents().get(0).getName(), "x2");

        // value 4
        assertEquals(nodes.get(2).getProbabilities().size(), 4);
        // test data would expect 1 here, but minimum likelyhood of its slibling forces it to 0.999
        assertEquals(nodes.get(2).getProbabilities().get(3).getP(), 0.999);
        assertEquals(nodes.get(2).getProbabilities().get(3).getLocalValue().getValue(), "1");
        assertEquals(nodes.get(2).getProbabilities().get(3).getLocalValue().getAttributeName(), "x3");
        assertEquals(nodes.get(2).getProbabilities().get(3).getParents().size(), 1);
        assertEquals(nodes.get(2).getProbabilities().get(3).getParents().get(0).getValue().getValue(), "1");
        assertEquals(nodes.get(2).getProbabilities().get(3).getParents().get(0).getName(), "x2");

        //assert there are two sets of sliblings as there is 1 parent with 2 unique values
        //Set 1=2 and set 3=4 are supposed to be equal due to the starting node used
        sliblings1 = Node.findSliblings(nodes.get(1).getProbabilities().get(0), nodes.get(1));
        sliblings2 = Node.findSliblings(nodes.get(1).getProbabilities().get(1), nodes.get(1));
        sliblings3 = Node.findSliblings(nodes.get(1).getProbabilities().get(2), nodes.get(1));
        sliblings4 = Node.findSliblings(nodes.get(1).getProbabilities().get(3), nodes.get(1));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertFalse(sliblings2.containsAll(sliblings3));
        assertTrue(sliblings3.containsAll(sliblings4));
        assertTrue(sliblings4.containsAll(sliblings3));
        sliblings1.addAll(sliblings3);
        assertTrue(sliblings1.containsAll(nodes.get(1).getProbabilities()));
    }

    @Test
    public void testMaximumLikelyhoodAlarm3Parents() {
        //small test to see if multiple parents with missing data are handled correctly
        // E.g. P1 = True, P2 = True never occurs in the trainingset, but P1 = True, P2 = False and P1= False, 
        // P2=True do occur so P1= True, P2 = True still needs to be handled as a potential parent

        String FIRSTHALF_ALARM = "resources/Experiments/alarm/Alarm10k_firsthalf.csv";
        String SECONDHALF_ALARM = "resources/Experiments/alarm/Alarm10k_secondhalf.csv";
        BayesServer station1 = new BayesServer(FIRSTHALF_ALARM, "1");
        BayesServer station2 = new BayesServer(SECONDHALF_ALARM, "2");

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
        List<WebNode> WebNodes = buildSmallAlarmNetwork();
        MaximumLikelyhoodRequest req = new MaximumLikelyhoodRequest();
        req.setNodes(WebNodes);
        List<Node> nodes = central.maximumLikelyhood(req);

        // find the ventlung node, it's the one with three parents
        Node vntl = null;
        for (Node n : nodes) {
            if (n.getName().equals("VENTLUNG")) {
                vntl = n;
                break;
            }
        }

        assertEquals(vntl.getParents().size(), 3);
        assertEquals(vntl.getUniquevalues().size(), 4);
        //total parent values:
        int uniqueParentCombos = 1;
        for (Node parent : vntl.getParents()) {
            uniqueParentCombos *= parent.getUniquevalues().size();
        }
        assertEquals(uniqueParentCombos, 24);
        assertEquals(uniqueParentCombos * vntl.getUniquevalues().size(), vntl.getProbabilities().size());
        Map<String, List<Theta>> sliblings = new HashMap<>();
        for (Theta t : vntl.getProbabilities()) {
            // each group of sliblings should consists of 4 sliblings
            List<Theta> slib = Node.findSliblings(t, vntl);
            String key = "";
            for (ParentValue p : t.getParents()) {
                key += p.getName() + " " + p.getValue().getValue() + " ";
            }
            sliblings.put(key, slib);
            // each set of sliblings should be as large as the unique values in the node
            assertEquals(slib.size(), vntl.getUniquevalues().size());
            // each theta should be > 0.0
            assertTrue(t.getP() > 0.0);
        }
        // there should be as many unique sets of sliblings as there are parent combos
        assertEquals(sliblings.size(), uniqueParentCombos);

        //all sets of sliblings should sum to 1
        //A small error due to machine precision is acceptable
        for (String key : sliblings.keySet()) {
            double sum = 0;
            for (Theta t : sliblings.get(key)) {
                sum += t.getP();
            }
            assertEquals(sum, 1.0, 0.01);
        }
    }

    private List<WebNode> buildSmallAlarmNetwork() {
        WebNode vtub = createWebNode("VENTTUBE", Attribute.AttributeType.string, new ArrayList<>());
        WebNode kink = createWebNode("KINKEDTUBE", Attribute.AttributeType.string, new ArrayList<>());
        WebNode inT = createWebNode("INTUBATION", Attribute.AttributeType.string, new ArrayList<>());
        WebNode vlng = createWebNode("VENTLUNG", Attribute.AttributeType.string,
                                     Arrays.asList(inT.getName(), kink.getName(), vtub.getName()));
        //list nodes in the order you want the attributes printed
        return Arrays.asList(vtub, kink, inT, vlng);
    }

    private WebNode createWebNode(String name, Attribute.AttributeType type, List<String> parents) {
        WebNode n = new WebNode();
        n.setType(type);
        n.setName(name);
        n.setParents(parents);
        return n;
    }


}