package com.florian.vertibayes.bayes.webservice;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.webservice.mapping.WebNodeMapper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class VertiBayesCentralServerTest {

    @Test
    public void testMaximumLikelyhoodBinned() {
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
        List<WebNode> webNodes = central.buildNetwork().getNodes();
        //add simple bins. The bins will essentially just form the binary division again
        // Since the bins will follow the same distribution we know the expected probabilities
        Bin zero = new Bin();
        zero.setLowerLimit("-1");
        zero.setUpperLimit("0.5");

        Bin one = new Bin();
        one.setLowerLimit("0.5");
        one.setUpperLimit("1.5");
        for (WebNode node : webNodes) {
            node.getBins().add(zero);
            node.getBins().add(one);
            node.setDiscrete(false);
        }
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);

        List<Node> nodes = WebNodeMapper.mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

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
        // put them in a map to easily find correct probability:
        HashMap<String, Theta> map = new HashMap<>();
        for (Theta t : nodes.get(0).getProbabilities()) {
            String key = t.getLocalRequirement().getLowerLimit().getValue() + ";" + t.getLocalRequirement()
                    .getUpperLimit()
                    .getValue();
            map.put(key, t);
        }

        // value 1 key: -1;0.5 which corresponds to the range of the first bin
        assertEquals(map.get("-1;0.5").getP(), 0.5);
        assertEquals(map.get("-1;0.5").getLocalRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5").getLocalRequirement().getName(), "x1");
        assertEquals(map.get("-1;0.5").getParents().size(), 0);

        // value 2 0.5;1.5 which corresponds to the range of the first bin
        assertEquals(map.get("0.5;1.5").getP(), 0.5);
        assertEquals(map.get("0.5;1.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5").getLocalRequirement().getName(), "x1");
        assertEquals(map.get("0.5;1.5").getParents().size(), 0);


        //assert all probabilities are viewed as sliblings as there are no parents
        List<Theta> sliblings = Node.findSliblings(nodes.get(0).getProbabilities().get(0), nodes.get(0));
        assertTrue(nodes.get(0).getProbabilities().containsAll(sliblings));
        assertTrue(sliblings.containsAll(nodes.get(0).getProbabilities()));

        //node 2 has 2 local values and 1 parent with 2 values resulting in 4 values in total
        // value 1
        assertEquals(nodes.get(1).getProbabilities().size(), 4);
        // put them in a map to easily find correct probability:
        map = new HashMap<>();
        for (Theta t : nodes.get(1).getProbabilities()) {
            String key = t.getLocalRequirement().getLowerLimit().getValue() + ";" + t.getLocalRequirement()
                    .getUpperLimit().getValue() + "p" + t.getParents().get(0).getRequirement()
                    .getLowerLimit().getValue() + ";" + t.getParents().get(0).getRequirement().getUpperLimit()
                    .getValue();
            map.put(key, t);
        }

        //keys: <child range>p<parent range>
        assertEquals(map.get("-1;0.5p-1;0.5").getP(), 0.8);
        assertEquals(map.get("-1;0.5p-1;0.5").getLocalRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p-1;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p-1;0.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("-1;0.5p-1;0.5").getParents().size(), 1);
        assertEquals(map.get("-1;0.5p-1;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p-1;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p-1;0.5").getParents().get(0).getName(), "x1");

        assertEquals(map.get("0.5;1.5p-1;0.5").getP(), 0.2);
        assertEquals(map.get("0.5;1.5p-1;0.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p-1;0.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p-1;0.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("0.5;1.5p-1;0.5").getParents().size(), 1);
        assertEquals(map.get("0.5;1.5p-1;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("0.5;1.5p-1;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p-1;0.5").getParents().get(0).getName(), "x1");

        assertEquals(map.get("-1;0.5p0.5;1.5").getP(), 0.2);
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().size(), 1);
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().get(0).getRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().get(0).getName(), "x1");

        assertEquals(map.get("0.5;1.5p0.5;1.5").getP(), 0.8);
        assertEquals(map.get("0.5;1.5p0.5;1.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getParents().size(), 1);
        assertEquals(map.get("0.5;1.5p0.5;1.5").getParents().get(0).getRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getParents().get(0).getName(), "x1");


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
        // put them in a map to easily find correct probability:
        map = new HashMap<>();
        for (Theta t : nodes.get(2).getProbabilities()) {
            String key = t.getLocalRequirement().getLowerLimit().getValue() + ";" + t.getLocalRequirement()
                    .getUpperLimit().getValue() + "p" + t.getParents().get(0).getRequirement()
                    .getLowerLimit().getValue() + ";" + t.getParents().get(0).getRequirement().getUpperLimit()
                    .getValue();
            map.put(key, t);
        }
        assertEquals(map.get("-1;0.5p-1;0.5").getP(), 0.8);
        assertEquals(map.get("-1;0.5p-1;0.5").getLocalRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p-1;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p-1;0.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("-1;0.5p-1;0.5").getParents().size(), 1);
        assertEquals(map.get("-1;0.5p-1;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p-1;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p-1;0.5").getParents().get(0).getName(), "x2");

        assertEquals(map.get("0.5;1.5p-1;0.5").getP(), 0.2);
        assertEquals(map.get("0.5;1.5p-1;0.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p-1;0.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p-1;0.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("0.5;1.5p-1;0.5").getParents().size(), 1);
        assertEquals(map.get("0.5;1.5p-1;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("0.5;1.5p-1;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p-1;0.5").getParents().get(0).getName(), "x2");

        assertEquals(map.get("-1;0.5p0.5;1.5").getP(), 0.001);
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().size(), 1);
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().get(0).getRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().get(0).getName(), "x2");

        assertEquals(map.get("0.5;1.5p0.5;1.5").getP(), 0.999);
        assertEquals(map.get("0.5;1.5p0.5;1.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getParents().size(), 1);
        assertEquals(map.get("0.5;1.5p0.5;1.5").getParents().get(0).getRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p0.5;1.5").getParents().get(0).getName(), "x2");

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
        List<WebNode> webNodes = central.buildNetwork().getNodes();
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);

        List<Node> nodes = WebNodeMapper.mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

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
        // put them in a map to easily find correct probability:
        HashMap<String, Theta> map = new HashMap<>();
        for (Theta t : nodes.get(0).getProbabilities()) {
            map.put(t.getLocalRequirement().getValue().getValue(), t);
        }

        // value 1
        assertEquals(map.get("0").getP(), 0.5);
        assertEquals(map.get("0").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("0").getLocalRequirement().getValue().getAttributeName(), "x1");
        assertEquals(map.get("0").getParents().size(), 0);

        // value 2
        assertEquals(map.get("1").getP(), 0.5);
        assertEquals(map.get("1").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("1").getLocalRequirement().getValue().getAttributeName(), "x1");
        assertEquals(map.get("1").getParents().size(), 0);

        //assert all probabilities are viewed as sliblings as there are no parents
        List<Theta> sliblings = Node.findSliblings(nodes.get(0).getProbabilities().get(0), nodes.get(0));
        assertTrue(nodes.get(0).getProbabilities().containsAll(sliblings));
        assertTrue(sliblings.containsAll(nodes.get(0).getProbabilities()));

        //node 2 has 2 local values and 1 parent with 2 values resulting in 4 values in total
        // value 1
        assertEquals(nodes.get(1).getProbabilities().size(), 4);
        // put them in a map to easily find correct probability:
        map = new HashMap<>();
        for (Theta t : nodes.get(1).getProbabilities()) {
            String key = t.getLocalRequirement().getValue().getValue() + t.getParents().get(0).getRequirement()
                    .getValue().getValue();
            map.put(key, t);
        }

        assertEquals(map.get("00").getP(), 0.8);
        assertEquals(map.get("00").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("00").getLocalRequirement().getValue().getAttributeName(), "x2");
        assertEquals(map.get("00").getParents().size(), 1);
        assertEquals(map.get("00").getParents().get(0).getRequirement().getValue().getValue(), "0");
        assertEquals(map.get("00").getParents().get(0).getName(), "x1");

        assertEquals(map.get("10").getP(), 0.2);
        assertEquals(map.get("10").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("10").getLocalRequirement().getValue().getAttributeName(), "x2");
        assertEquals(map.get("10").getParents().size(), 1);
        assertEquals(map.get("10").getParents().get(0).getRequirement().getValue().getValue(), "0");
        assertEquals(map.get("10").getParents().get(0).getName(), "x1");

        assertEquals(map.get("01").getP(), 0.2);
        assertEquals(map.get("01").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("01").getLocalRequirement().getValue().getAttributeName(), "x2");
        assertEquals(map.get("01").getParents().size(), 1);
        assertEquals(map.get("01").getParents().get(0).getRequirement().getValue().getValue(), "1");
        assertEquals(map.get("01").getParents().get(0).getName(), "x1");

        assertEquals(map.get("11").getP(), 0.8);
        assertEquals(map.get("11").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("11").getLocalRequirement().getValue().getAttributeName(), "x2");
        assertEquals(map.get("11").getParents().size(), 1);
        assertEquals(map.get("11").getParents().get(0).getRequirement().getValue().getValue(), "1");
        assertEquals(map.get("11").getParents().get(0).getName(), "x1");


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
        // put them in a map to easily find correct probability:
        map = new HashMap<>();
        for (Theta t : nodes.get(2).getProbabilities()) {
            String key = t.getLocalRequirement().getValue().getValue() + t.getParents().get(0).getRequirement()
                    .getValue().getValue();
            map.put(key, t);
        }
        assertEquals(map.get("00").getP(), 0.8);
        assertEquals(map.get("00").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("00").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("00").getParents().size(), 1);
        assertEquals(map.get("00").getParents().get(0).getRequirement().getValue().getValue(), "0");
        assertEquals(map.get("00").getParents().get(0).getName(), "x2");

        assertEquals(map.get("10").getP(), 0.2);
        assertEquals(map.get("10").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("10").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("10").getParents().size(), 1);
        assertEquals(map.get("10").getParents().get(0).getRequirement().getValue().getValue(), "0");
        assertEquals(map.get("10").getParents().get(0).getName(), "x2");

        assertEquals(map.get("01").getP(), 0.001);
        assertEquals(map.get("01").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("01").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("01").getParents().size(), 1);
        assertEquals(map.get("01").getParents().get(0).getRequirement().getValue().getValue(), "1");
        assertEquals(map.get("01").getParents().get(0).getName(), "x2");

        assertEquals(map.get("11").getP(), 0.999);
        assertEquals(map.get("11").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("11").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("11").getParents().size(), 1);
        assertEquals(map.get("11").getParents().get(0).getRequirement().getValue().getValue(), "1");
        assertEquals(map.get("11").getParents().get(0).getName(), "x2");

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
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(WebNodes);
        List<Node> nodes = WebNodeMapper.mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

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
                key += p.getName() + " " + p.getRequirement().getValue().getValue() + " ";
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