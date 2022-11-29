package com.florian.vertibayes.bayes.webservice;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.CreateNetworkRequest;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationResponse;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.florian.vertibayes.bayes.webservice.NetworkTest.createK2Nodes;
import static com.florian.vertibayes.webservice.mapping.WebNodeMapper.mapWebNodeToNode;
import static org.junit.jupiter.api.Assertions.*;

public class VertiBayesCentralServerTest {

    @Test
    public void testMaximumLikelyhoodAsia3PartiesKFold() throws Exception {
        //Small test with 3 parties.
        //1 node has no parents, so requires only 1 party
        //1 node has 1 parent, requires 2 parties
        //1 node has 2 parents, requires all 3 parties

        //Using maximumlikelyhood because the small network size means EM/synthetic generation fluctuates wildly
        // between runs, but maximumlikelyhood is stable, and the point of htis test is to test the federated bit

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

        List<WebNode> WebNodes = buildSmallAsiaNetwork();
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(WebNodes);
        req.setTarget("asia");
        req.setFolds(3);

        VertiBayesCentralServer central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2, endpoint3), secretEnd);
        ExpectationMaximizationResponse res = central.expectationMaximization(
                req);
        req.setFolds(1);
        ExpectationMaximizationResponse resNoFolds =
                central.expectationMaximization(
                        req);

        //assert the averageAUC, which is bad cuz it's a model with 3 random nodes.
        assertEquals(res.getSvdgAuc(), 0.50, 0.1);

        List<Node> nodes = mapWebNodeToNode(res.getNodes());
        List<Node> nodesNoFolds = mapWebNodeToNode(resNoFolds.getNodes());

        // find the ventlung node, it's the one with three parents
        Node asia = null;
        for (Node n : nodes) {
            if (n.getName().equals("asia")) {
                asia = n;
                break;
            }
        }

        Node asiaNoFold = null;
        for (Node n : nodesNoFolds) {
            if (n.getName().equals("asia")) {
                asiaNoFold = n;
                break;
            }
        }

        assertEquals(asia.getParents().size(), 0);
        assertEquals(asia.getUniquevalues().size(), 2);
        assertEquals(asia.getProbabilities().size(), 2);
        //assert the final probabilities are the same between the final model and a model trained with no folds
        assertEquals(asia.getProbabilities().get(0).getP(), asiaNoFold.getProbabilities().get(0).getP(), 0.01);
        assertEquals(asia.getProbabilities().get(1).getP(), asiaNoFold.getProbabilities().get(1).getP(), 0.01);


        Node either = null;
        for (Node n : nodes) {
            if (n.getName().equals("either")) {
                either = n;
                break;
            }
        }

        Node eitherNoFold = null;
        for (Node n : nodes) {
            if (n.getName().equals("either")) {
                eitherNoFold = n;
                break;
            }
        }

        assertEquals(either.getParents().size(), 1);
        assertEquals(either.getUniquevalues().size(), 2);
        assertEquals(either.getProbabilities().size(), 4);
        for (int i = 0; i < 4; i++) {
            assertEquals(either.getProbabilities().get(i).getP(), eitherNoFold.getProbabilities().get(i).getP(), 0.01);
        }


        Node lung = null;
        for (Node n : nodes) {
            if (n.getName().equals("lung")) {
                lung = n;
                break;
            }
        }

        Node lungNoFold = null;
        for (Node n : nodes) {
            if (n.getName().equals("lung")) {
                lungNoFold = n;
                break;
            }
        }

        assertEquals(lung.getParents().size(), 2);
        assertEquals(lung.getUniquevalues().size(), 2);
        assertEquals(lung.getProbabilities().size(), 8);
        for (int i = 0; i < 8; i++) {
            assertEquals(lung.getProbabilities().get(i).getP(), lungNoFold.getProbabilities().get(i).getP(), 0.01);
        }
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

        //hybrid
        VertiBayesCentralServer central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint3), secretEnd);
        endpoint1.setUseLocalOnly(true);

        CreateNetworkRequest req = new CreateNetworkRequest();
        req.setMinPercentage(10);
        List<Node> webNodesHybrid = mapWebNodeToNode(central.maximumLikelyhood(central.buildNetwork(req)).getNodes());

        // check if it matches expected network
        assertEquals(webNodesHybrid.size(), 3);
        assertEquals(webNodesHybrid.get(0).getParents().size(), 0);
        assertEquals(webNodesHybrid.get(1).getParents().size(), 1);
        assertTrue(webNodesHybrid.get(1).getParents().contains(webNodesHybrid.get(0)));
        assertEquals(webNodesHybrid.get(2).getParents().size(), 1);
        assertTrue(webNodesHybrid.get(2).getParents().contains(webNodesHybrid.get(1)));

        //normal
        central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2, endpoint3), secretEnd);
        endpoint1.setUseLocalOnly(false);
        CreateNetworkRequest networkRequest = new CreateNetworkRequest();
        req.setMinPercentage(10);
        List<Node> webNodes = mapWebNodeToNode(
                central.maximumLikelyhood(central.buildNetwork(networkRequest)).getNodes());

        // check if it matches expected network
        assertEquals(webNodes.size(), 3);
        assertEquals(webNodes.get(0).getParents().size(), 0);
        assertEquals(webNodes.get(1).getParents().size(), 1);
        assertTrue(webNodes.get(1).getParents().contains(webNodes.get(0)));
        assertEquals(webNodes.get(2).getParents().size(), 1);
        assertTrue(webNodes.get(2).getParents().contains(webNodes.get(1)));
        //expected network an example are based on the example in "resources/k2_algorithm.pdf"

        //The structures were the same, but the probabilities are different, check the first probability to make sure.
        assertEquals(webNodes.get(0).getProbabilities().get(0).getP(), 0.5);
        assertEquals(webNodesHybrid.get(0).getProbabilities().get(0).getP(), 0.4);
    }

    @Test
    public void testDoubleSplitManuallyBinned() {
        BayesServer station1 = new BayesServer("resources/Experiments/doublesplit/smallK2Example_firsthalf.csv",
                                               "1");
        BayesServer station2 = new BayesServer("resources/Experiments/doublesplit/smallK2Example_secondhalf.csv", "2");
        BayesServer station3 = new BayesServer(
                "resources/Experiments/doublesplit/smallK2Example_secondhalf_morepopulation.csv", "3");

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

        VertiBayesCentralServer central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2, endpoint3), secretEnd);

        CreateNetworkRequest networkRequest = new CreateNetworkRequest();
        networkRequest.setMinPercentage(10);
        networkRequest.setNodes(createK2Nodes());
        List<WebNode> webNodes = central.buildNetwork(networkRequest).getNodes();
        //add simple bins. The bins will essentially just form the binary division again
        // Since the bins will follow the same distribution we know the expected probabilities

        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);

        Bin zero = new Bin();
        zero.setLowerLimit("-1");
        zero.setUpperLimit("0.5");

        Bin one = new Bin();
        one.setLowerLimit("0.5");
        one.setUpperLimit("1.5");

        for (WebNode n : webNodes) {
            if (n.getName().equals("x3")) {
                n.getBins().add(zero);
                n.getBins().add(one);
                n.setType(Attribute.AttributeType.real);
            }
        }

        List<Node> nodes = mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

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
        assertEquals(map.get("-0.5;0.5").getP(), 0.5);
        assertEquals(map.get("-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "-0.5");
        assertEquals(map.get("-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-0.5;0.5").getLocalRequirement().getName(), "x1");
        assertEquals(map.get("-0.5;0.5").getParents().size(), 0);

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
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getP(), 0.8);
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "-0.5");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getParents().size(), 1);
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "-0.5");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "0.5");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getParents().get(0).getName(), "x1");

        assertEquals(map.get("0.5;1.5p-0.5;0.5").getP(), 0.2);
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().size(), 1);
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "-0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getName(), "x1");

        assertEquals(map.get("-0.5;0.5p0.5;1.5").getP(), 0.2);
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getLocalRequirement().getLowerLimit().getValue(), "-0.5");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getParents().size(), 1);
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "0.5");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "1.5");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getParents().get(0).getName(), "x1");

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
        assertEquals(map.get("-1;0.5p-0.5;0.5").getP(), 0.8);
        assertEquals(map.get("-1;0.5p-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getParents().size(), 1);
        assertEquals(map.get("-1;0.5p-0.5;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "-0.5");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getParents().get(0).getName(), "x2");

        assertEquals(map.get("0.5;1.5p-0.5;0.5").getP(), 0.2);
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().size(), 1);
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "-0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getName(), "x2");

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
        sliblings1 = Node.findSliblings(nodes.get(2).getProbabilities().get(0), nodes.get(2));
        sliblings2 = Node.findSliblings(nodes.get(2).getProbabilities().get(1), nodes.get(2));
        sliblings3 = Node.findSliblings(nodes.get(2).getProbabilities().get(2), nodes.get(2));
        sliblings4 = Node.findSliblings(nodes.get(2).getProbabilities().get(3), nodes.get(2));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertFalse(sliblings2.containsAll(sliblings3));
        assertTrue(sliblings3.containsAll(sliblings4));
        assertTrue(sliblings4.containsAll(sliblings3));
        sliblings1.addAll(sliblings3);
        assertTrue(sliblings1.containsAll(nodes.get(2).getProbabilities()));
    }

    @Test
    public void testMaximumLikelyhoodManuallyBinned() {
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
        CreateNetworkRequest networkRequest = new CreateNetworkRequest();
        networkRequest.setMinPercentage(10);
        networkRequest.setNodes(createK2Nodes());
        List<WebNode> webNodes = central.buildNetwork(networkRequest).getNodes();
        //add simple bins. The bins will essentially just form the binary division again
        // Since the bins will follow the same distribution we know the expected probabilities
        Bin zero = new Bin();
        zero.setLowerLimit("-1");
        zero.setUpperLimit("0.5");

        Bin one = new Bin();
        one.setLowerLimit("0.5");
        one.setUpperLimit("1.5");


        // no unknown bin, as this test does not deal with unknowns
        for (WebNode node : webNodes) {
            if (node.getName().equals("x3")) {
                node.getBins().add(zero);
                node.getBins().add(one);
                node.setType(Attribute.AttributeType.real);
            }
        }
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);

        List<Node> nodes = mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

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
        assertEquals(map.get("-0.5;0.5").getP(), 0.5);
        assertEquals(map.get("-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "-0.5");
        assertEquals(map.get("-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-0.5;0.5").getLocalRequirement().getName(), "x1");
        assertEquals(map.get("-0.5;0.5").getParents().size(), 0);

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
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getP(), 0.8);
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "-0.5");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getParents().size(), 1);
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "-0.5");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "0.5");
        assertEquals(map.get("-0.5;0.5p-0.5;0.5").getParents().get(0).getName(), "x1");

        assertEquals(map.get("0.5;1.5p-0.5;0.5").getP(), 0.2);
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().size(), 1);
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "-0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getName(), "x1");

        assertEquals(map.get("-0.5;0.5p0.5;1.5").getP(), 0.2);
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getLocalRequirement().getLowerLimit().getValue(), "-0.5");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getParents().size(), 1);
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "0.5");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "1.5");
        assertEquals(map.get("-0.5;0.5p0.5;1.5").getParents().get(0).getName(), "x1");

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
        assertEquals(map.get("-1;0.5p-0.5;0.5").getP(), 0.8);
        assertEquals(map.get("-1;0.5p-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getParents().size(), 1);
        assertEquals(map.get("-1;0.5p-0.5;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "-0.5");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "0.5");
        assertEquals(map.get("-1;0.5p-0.5;0.5").getParents().get(0).getName(), "x2");

        assertEquals(map.get("0.5;1.5p-0.5;0.5").getP(), 0.2);
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getLowerLimit().getValue(), "0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getUpperLimit().getValue(), "1.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().size(), 1);
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "-0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "0.5");
        assertEquals(map.get("0.5;1.5p-0.5;0.5").getParents().get(0).getName(), "x2");

        assertEquals(map.get("-1;0.5p0.5;1.5").getP(), 0.001);
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getLowerLimit().getValue(), "-1");
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getUpperLimit().getValue(), "0.5");
        assertEquals(map.get("-1;0.5p0.5;1.5").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().size(), 1);
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().get(0).getRequirement().getLowerLimit().getValue(),
                     "0.5");
        assertEquals(map.get("-1;0.5p0.5;1.5").getParents().get(0).getRequirement().getUpperLimit().getValue(),
                     "1.5");
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
        sliblings1 = Node.findSliblings(nodes.get(2).getProbabilities().get(0), nodes.get(2));
        sliblings2 = Node.findSliblings(nodes.get(2).getProbabilities().get(1), nodes.get(2));
        sliblings3 = Node.findSliblings(nodes.get(2).getProbabilities().get(2), nodes.get(2));
        sliblings4 = Node.findSliblings(nodes.get(2).getProbabilities().get(3), nodes.get(2));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertFalse(sliblings2.containsAll(sliblings3));
        assertTrue(sliblings3.containsAll(sliblings4));
        assertTrue(sliblings4.containsAll(sliblings3));
        sliblings1.addAll(sliblings3);
        assertTrue(sliblings1.containsAll(nodes.get(2).getProbabilities()));
    }


    @Test
    public void testMaximumLikelyhoodAutomaticBinNumeric() {
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
        CreateNetworkRequest networkRequest = new CreateNetworkRequest();
        networkRequest.setMinPercentage(10);
        networkRequest.setNodes(createK2Nodes());
        List<WebNode> webNodes = central.buildNetwork(networkRequest).getNodes();
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);
        for (WebNode n : webNodes) {
            n.setBins(new HashSet<>());
        }

        List<Node> nodes = mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

        // check if it matches expected network
        assertEquals(nodes.size(), 3);
        assertEquals(nodes.get(0).getParents().size(), 0);
        assertEquals(nodes.get(1).getParents().size(), 1);
        assertTrue(nodes.get(1).getParents().contains(nodes.get(0)));
        assertEquals(nodes.get(2).getParents().size(), 1);
        assertTrue(nodes.get(2).getParents().contains(nodes.get(1)));
        //expected network an example are based on the example in "resources/k2_algorithm.pdf"

        // now check the thetas
        // node 1 has 2 values and no parents
        // However, it's binned into 1 bin due to the low count
        assertEquals(nodes.get(0).getProbabilities().size(), 1);
        // put them in a map to easily find correct probability:
        HashMap<String, Theta> map = new HashMap<>();
        for (Theta t : nodes.get(0).getProbabilities()) {
            String key = t.getLocalRequirement().getUpperLimit().getValue() + t.getLocalRequirement().getLowerLimit()
                    .getValue();
            map.put(key, t);
        }

        assertEquals(map.get("20").getP(), 1.0);
        assertEquals(map.get("20").getLocalRequirement().getLowerLimit().getValue(), "0");
        assertEquals(map.get("20").getLocalRequirement().getUpperLimit().getValue(), "2");
        assertEquals(map.get("20").getLocalRequirement().getUpperLimit().getAttributeName(), "x1");
        assertEquals(map.get("20").getParents().size(), 0);

        //assert all probabilities are viewed as sliblings as there are no parents
        List<Theta> sliblings = Node.findSliblings(nodes.get(0).getProbabilities().get(0), nodes.get(0));
        assertTrue(nodes.get(0).getProbabilities().containsAll(sliblings));
        assertTrue(sliblings.containsAll(nodes.get(0).getProbabilities()));

        //node 2 has 2 local values, binned into 1 bin and 1 parent with 1 bin resulting in 1 values in total
        // value 1
        assertEquals(nodes.get(1).getProbabilities().size(), 1);
        // put them in a map to easily find correct probability:
        map = new HashMap<>();
        for (Theta t : nodes.get(1).getProbabilities()) {
            String key = t.getLocalRequirement().getLowerLimit().getValue() +
                    t.getLocalRequirement().getUpperLimit().getValue() + t.getParents().get(0).getRequirement()
                    .getLowerLimit().getValue() + t.getParents().get(0).getRequirement()
                    .getUpperLimit().getValue();
            map.put(key, t);
        }

        assertEquals(map.get("0202").getP(), 1.0);
        assertEquals(map.get("0202").getLocalRequirement().getLowerLimit().getValue(), "0");
        assertEquals(map.get("0202").getLocalRequirement().getUpperLimit().getValue(), "2");
        assertEquals(map.get("0202").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("0202").getParents().size(), 1);
        assertEquals(map.get("0202").getParents().get(0).getRequirement().getLowerLimit().getValue(), "0");
        assertEquals(map.get("0202").getParents().get(0).getRequirement().getUpperLimit().getValue(), "2");
        assertEquals(map.get("0202").getParents().get(0).getName(), "x1");

        //assert there is one sets of sliblings
        List<Theta> sliblings1 = Node.findSliblings(nodes.get(1).getProbabilities().get(0), nodes.get(1));
        assertTrue(sliblings1.containsAll(nodes.get(1).getProbabilities()));

        //node 3 has 2 local values and 1 parent with 1 bin resulting in 2 values in total
        // put them in a map to easily find correct probability:
        map = new HashMap<>();
        for (Theta t : nodes.get(2).getProbabilities()) {
            String key = t.getLocalRequirement().getValue().getValue() + t.getParents().get(0).getRequirement()
                    .getLowerLimit().getValue() + t.getParents().get(0).getRequirement()
                    .getUpperLimit().getValue();
            map.put(key, t);
        }
        assertEquals(map.get("002").getP(), 0.8);
        assertEquals(map.get("002").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("002").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("002").getParents().size(), 1);
        assertEquals(map.get("002").getParents().get(0).getRequirement().getLowerLimit().getValue(), "0");
        assertEquals(map.get("002").getParents().get(0).getRequirement().getUpperLimit().getValue(), "2");
        assertEquals(map.get("002").getParents().get(0).getName(), "x2");

        assertEquals(map.get("102").getP(), 0.2);
        assertEquals(map.get("102").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("102").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("102").getParents().size(), 1);
        assertEquals(map.get("102").getParents().get(0).getRequirement().getLowerLimit().getValue(), "0");
        assertEquals(map.get("102").getParents().get(0).getRequirement().getUpperLimit().getValue(), "2");
        assertEquals(map.get("102").getParents().get(0).getName(), "x2");

        //assert there are two sets of sliblings as there is 1 parent with 2 unique values
        //Set 1=2 and set 3=4 are supposed to be equal due to the starting node used
        sliblings1 = Node.findSliblings(nodes.get(2).getProbabilities().get(0), nodes.get(2));
        List<Theta> sliblings2 = Node.findSliblings(nodes.get(2).getProbabilities().get(1), nodes.get(2));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertTrue(sliblings1.containsAll(nodes.get(2).getProbabilities()));
    }

    @Test
    public void testMaximumLikelyhoodNoBin() {
        BayesServer station1 = new BayesServer("resources/Experiments/k2/smallK2Example_firsthalfString.csv", "1");
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
        CreateNetworkRequest networkRequest = new CreateNetworkRequest();
        networkRequest.setMinPercentage(10);
        List<WebNode> webNodes = central.buildNetwork(networkRequest).getNodes();

        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);

        List<Node> nodes = mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

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
        sliblings1 = Node.findSliblings(nodes.get(2).getProbabilities().get(0), nodes.get(2));
        sliblings2 = Node.findSliblings(nodes.get(2).getProbabilities().get(1), nodes.get(2));
        sliblings3 = Node.findSliblings(nodes.get(2).getProbabilities().get(2), nodes.get(2));
        sliblings4 = Node.findSliblings(nodes.get(2).getProbabilities().get(3), nodes.get(2));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertFalse(sliblings2.containsAll(sliblings3));
        assertTrue(sliblings3.containsAll(sliblings4));
        assertTrue(sliblings4.containsAll(sliblings3));
        sliblings1.addAll(sliblings3);
        assertTrue(sliblings1.containsAll(nodes.get(2).getProbabilities()));
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
        List<Node> nodes = mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

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

    @Test
    public void testExpectationMaximizationhoodBinned() throws Exception {
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
        CreateNetworkRequest networkRequest = new CreateNetworkRequest();
        networkRequest.setMinPercentage(10);
        networkRequest.setNodes(createK2Nodes());
        List<WebNode> webNodes = central.buildNetwork(networkRequest).getNodes();
        //add simple bins"to all nodes expect X3. The bins will essentially just form the binary division again
        // Since the bins will follow the same distribution we know the expected probabilities

        Bin unknown = new Bin();
        unknown.setLowerLimit("?");
        unknown.setUpperLimit("?");
        for (WebNode node : webNodes) {
            if (!node.getName().equals("x3")) {
                node.getBins().add(unknown);
            }
        }
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);
        req.setTarget("x3");


        List<Node> nodes = mapWebNodeToNode(central.expectationMaximization(req).getNodes());

        // check if it matches expected network
        assertEquals(nodes.size(), 3);
        assertEquals(nodes.get(0).getParents().size(), 0);
        assertEquals(nodes.get(1).getParents().size(), 1);
        assertTrue(nodes.get(1).getParents().contains(nodes.get(0)));
        assertEquals(nodes.get(2).getParents().size(), 1);
        assertTrue(nodes.get(2).getParents().contains(nodes.get(1)));
        //expected network an example are based on the example in "resources/k2_algorithm.pdf"

        // now check the thetas
        // node 1 has 2 values and no parents, however, due to binning it's become 1 bin
        assertEquals(nodes.get(0).getProbabilities().size(), 1);
        // put them in a map to easily find correct probability:
        HashMap<String, Theta> map = new HashMap<>();
        //using the -inf and inf values to generate keys given that the other values are suspect to slight randomness.
        for (Theta t : nodes.get(0).getProbabilities()) {
            if (t.getLocalRequirement().isRange()) {
                if (t.getLocalRequirement().getLowerLimit().getValue().equals("-inf")) {
                    String key = t.getLocalRequirement().getLowerLimit().getValue();
                    map.put(key, t);
                } else {
                    String key = t.getLocalRequirement().getUpperLimit().getValue();
                    map.put(key, t);
                }
            }
        }


        assertEquals(map.get("All").getP(), 1.0, 0.05);
        assertEquals(map.get("All").getLocalRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("All").getLocalRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("All").getLocalRequirement().getName(), "x1");
        assertEquals(map.get("All").getParents().size(), 0);


        //assert all probabilities are viewed as sliblings as there are no parents
        List<Theta> sliblings = Node.findSliblings(nodes.get(0).getProbabilities().get(0), nodes.get(0));
        assertTrue(nodes.get(0).getProbabilities().containsAll(sliblings));
        assertTrue(sliblings.containsAll(nodes.get(0).getProbabilities()));

        //node 2 has 2 local values, which are binned into 1 box and 1 parent with1 binn resulting in 1 in total
        // value 1
        assertEquals(nodes.get(1).getProbabilities().size(), 1);
        // put them in a map to easily find correct probability:
        //using the -inf and inf values to generate keys given that the other values are suspect to slight randomness.
        map = new HashMap<>();
        for (Theta t : nodes.get(1).getProbabilities()) {
            String key = "";
            if (t.getLocalRequirement().getLowerLimit().getValue().equals("-inf")) {
                key += t.getLocalRequirement().getLowerLimit().getValue();

            } else {
                key += t.getLocalRequirement().getUpperLimit().getValue();

            }
            if (t.getParents().get(0).getRequirement().getLowerLimit().getValue().equals("-inf")) {
                key += t.getParents().get(0).getRequirement().getLowerLimit().getValue();

            } else {
                key += t.getParents().get(0).getRequirement().getUpperLimit().getValue();

            }
            map.put(key, t);
        }

        //keys: <child range>p<parent range>
        assertEquals(map.get("AllAll").getP(), 1.0, 0.05);
        assertEquals(map.get("AllAll").getLocalRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("AllAll").getLocalRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("AllAll").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("AllAll").getParents().size(), 1);
        assertEquals(map.get("AllAll").getParents().get(0).getRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("AllAll").getParents().get(0).getRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("AllAll").getParents().get(0).getName(), "x1");

        //assert there is set of sliblings as there is 1 parent with 1 bin
        //Set 1=2 and set 3=4 are supposed to be equal due to the starting node used
        List<Theta> sliblings1 = Node.findSliblings(nodes.get(1).getProbabilities().get(0), nodes.get(1));
        assertTrue(sliblings1.containsAll(nodes.get(1).getProbabilities()));

        //node 3 has 2 local values and 1 parent with 1 bin resulting in 2 values in total
        // put them in a map to easily find correct probability:
        map = new HashMap<>();
        for (Theta t : nodes.get(2).getProbabilities()) {
            String key = t.getLocalRequirement().getValue().getValue();
            if (t.getParents().get(0).getRequirement().getLowerLimit().getValue().equals("-inf")) {
                key += t.getParents().get(0).getRequirement().getLowerLimit().getValue();

            } else {
                key += t.getParents().get(0).getRequirement().getUpperLimit().getValue();

            }
            map.put(key, t);
        }
        assertEquals(map.get("0All").getP(), 0.5, 0.05);
        assertEquals(map.get("0All").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("0All").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("0All").getParents().size(), 1);
        assertEquals(map.get("0All").getParents().get(0).getRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("0All").getParents().get(0).getRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("0All").getParents().get(0).getName(), "x2");

        assertEquals(map.get("1All").getP(), 0.5, 0.05);
        assertEquals(map.get("1All").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("1All").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("1All").getParents().size(), 1);
        assertEquals(map.get("1All").getParents().get(0).getRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("1All").getParents().get(0).getRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("1All").getParents().get(0).getName(), "x2");

        //assert there is one set of sliblings as there is 1 parent with 1 bin
        sliblings1 = Node.findSliblings(nodes.get(2).getProbabilities().get(0), nodes.get(2));
        List<Theta> sliblings2 = Node.findSliblings(nodes.get(2).getProbabilities().get(1), nodes.get(2));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertTrue(sliblings1.containsAll(nodes.get(2).getProbabilities()));
    }

    @Test
    public void testExpectationMaximizationhoodManuallyBinnedWithUnknownValues() throws Exception {
        BayesServer station1 = new BayesServer("resources/Experiments/k2/smallK2Example_firsthalfMissing.csv", "1");
        BayesServer station2 = new BayesServer("resources/Experiments/k2/smallK2Example_secondhalfMissing.csv", "2");

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
        CreateNetworkRequest networkRequest = new CreateNetworkRequest();
        networkRequest.setMinPercentage(10);
        networkRequest.setNodes(createK2Nodes());

        Bin unknown = new Bin();
        unknown.setLowerLimit("?");
        unknown.setUpperLimit("?");
        for (WebNode node : networkRequest.getNodes()) {
            if (!node.getName().equals("x3")) {
                node.getBins().add(unknown);
            }
        }
        List<WebNode> webNodes = central.buildNetwork(networkRequest).getNodes();
        //add simple bins"to all nodes expect X3. The bins will essentially just form the binary division again
        // Since the bins will follow the same distribution we know the expected probabilities

        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);
        req.setTarget("x3");

        List<Node> nodes = mapWebNodeToNode(central.expectationMaximization(req).getNodes());

        // check if it matches expected network
        assertEquals(nodes.size(), 3);
        assertEquals(nodes.get(0).getParents().size(), 0);
        assertEquals(nodes.get(1).getParents().size(), 1);
        assertTrue(nodes.get(1).getParents().contains(nodes.get(0)));
        assertEquals(nodes.get(2).getParents().size(), 1);
        assertTrue(nodes.get(2).getParents().contains(nodes.get(1)));
        //expected network an example are based on the example in "resources/k2_algorithm.pdf"

        // now check the thetas
        // node 1 has 2 values and no parents, however, due to binning it's become 1 bin
        assertEquals(nodes.get(0).getProbabilities().size(), 1);
        // put them in a map to easily find correct probability:
        HashMap<String, Theta> map = new HashMap<>();
        //using the -inf and inf values to generate keys given that the other values are suspect to slight randomness.
        for (Theta t : nodes.get(0).getProbabilities()) {
            if (t.getLocalRequirement().isRange()) {
                if (t.getLocalRequirement().getLowerLimit().getValue().equals("-inf")) {
                    String key = t.getLocalRequirement().getLowerLimit().getValue();
                    map.put(key, t);
                } else {
                    String key = t.getLocalRequirement().getUpperLimit().getValue();
                    map.put(key, t);
                }
            }
        }


        assertEquals(map.get("All").getP(), 1.0, 0.05);
        assertEquals(map.get("All").getLocalRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("All").getLocalRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("All").getLocalRequirement().getName(), "x1");
        assertEquals(map.get("All").getParents().size(), 0);


        //assert all probabilities are viewed as sliblings as there are no parents
        List<Theta> sliblings = Node.findSliblings(nodes.get(0).getProbabilities().get(0), nodes.get(0));
        assertTrue(nodes.get(0).getProbabilities().containsAll(sliblings));
        assertTrue(sliblings.containsAll(nodes.get(0).getProbabilities()));

        //node 2 has 2 local values, which are binned into 1 box and 1 parent with1 binn resulting in 1 in total
        // value 1
        assertEquals(nodes.get(1).getProbabilities().size(), 1);
        // put them in a map to easily find correct probability:
        //using the -inf and inf values to generate keys given that the other values are suspect to slight randomness.
        map = new HashMap<>();
        for (Theta t : nodes.get(1).getProbabilities()) {
            String key = "";
            if (t.getLocalRequirement().getLowerLimit().getValue().equals("-inf")) {
                key += t.getLocalRequirement().getLowerLimit().getValue();

            } else {
                key += t.getLocalRequirement().getUpperLimit().getValue();

            }
            if (t.getParents().get(0).getRequirement().getLowerLimit().getValue().equals("-inf")) {
                key += t.getParents().get(0).getRequirement().getLowerLimit().getValue();

            } else {
                key += t.getParents().get(0).getRequirement().getUpperLimit().getValue();

            }
            map.put(key, t);
        }

        //keys: <child range>p<parent range>
        assertEquals(map.get("AllAll").getP(), 1.0, 0.05);
        assertEquals(map.get("AllAll").getLocalRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("AllAll").getLocalRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("AllAll").getLocalRequirement().getName(), "x2");
        assertEquals(map.get("AllAll").getParents().size(), 1);
        assertEquals(map.get("AllAll").getParents().get(0).getRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("AllAll").getParents().get(0).getRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("AllAll").getParents().get(0).getName(), "x1");

        //assert there is set of sliblings as there is 1 parent with 1 bin
        //Set 1=2 and set 3=4 are supposed to be equal due to the starting node used
        List<Theta> sliblings1 = Node.findSliblings(nodes.get(1).getProbabilities().get(0), nodes.get(1));
        assertTrue(sliblings1.containsAll(nodes.get(1).getProbabilities()));

        //node 3 has 2 local values and 1 parent with 1 bin resulting in 2 values in total
        // put them in a map to easily find correct probability:
        map = new HashMap<>();
        for (Theta t : nodes.get(2).getProbabilities()) {
            String key = t.getLocalRequirement().getValue().getValue();
            if (t.getParents().get(0).getRequirement().getLowerLimit().getValue().equals("-inf")) {
                key += t.getParents().get(0).getRequirement().getLowerLimit().getValue();

            } else {
                key += t.getParents().get(0).getRequirement().getUpperLimit().getValue();

            }
            map.put(key, t);
        }
        assertEquals(map.get("0All").getP(), 0.36, 0.05);
        assertEquals(map.get("0All").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("0All").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("0All").getParents().size(), 1);
        assertEquals(map.get("0All").getParents().get(0).getRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("0All").getParents().get(0).getRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("0All").getParents().get(0).getName(), "x2");

        assertEquals(map.get("1All").getP(), 0.65, 0.05);
        assertEquals(map.get("1All").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("1All").getLocalRequirement().getName(), "x3");
        assertEquals(map.get("1All").getParents().size(), 1);
        assertEquals(map.get("1All").getParents().get(0).getRequirement().getLowerLimit().getValue(), "All");
        assertEquals(map.get("1All").getParents().get(0).getRequirement().getUpperLimit().getValue(), "All");
        assertEquals(map.get("1All").getParents().get(0).getName(), "x2");

        //assert there is one set of sliblings as there is 1 parent with 1 bin
        sliblings1 = Node.findSliblings(nodes.get(2).getProbabilities().get(0), nodes.get(2));
        List<Theta> sliblings2 = Node.findSliblings(nodes.get(2).getProbabilities().get(1), nodes.get(2));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertTrue(sliblings1.containsAll(nodes.get(2).getProbabilities()));
    }


    @Test
    public void testExpectationMaximization() throws Exception {
        BayesServer station1 = new BayesServer("resources/Experiments/k2/smallK2Example_firsthalfString.csv", "1");
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
        CreateNetworkRequest networkRequest = new CreateNetworkRequest();
        networkRequest.setMinPercentage(10);
        List<WebNode> webNodes = central.buildNetwork(networkRequest).getNodes();
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);
        req.setTarget("x3");

        List<Node> nodes = mapWebNodeToNode(central.expectationMaximization(req).getNodes());

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
        assertEquals(map.get("0").getP(), 0.5, 0.05);
        assertEquals(map.get("0").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("0").getLocalRequirement().getValue().getAttributeName(), "x1");
        assertEquals(map.get("0").getParents().size(), 0);

        // value 2
        assertEquals(map.get("1").getP(), 0.5, 0.05);
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

        assertEquals(map.get("00").getP(), 0.8, 0.05);
        assertEquals(map.get("00").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("00").getLocalRequirement().getValue().getAttributeName(), "x2");
        assertEquals(map.get("00").getParents().size(), 1);
        assertEquals(map.get("00").getParents().get(0).getRequirement().getValue().getValue(), "0");
        assertEquals(map.get("00").getParents().get(0).getName(), "x1");

        assertEquals(map.get("10").getP(), 0.2, 0.05);
        assertEquals(map.get("10").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("10").getLocalRequirement().getValue().getAttributeName(), "x2");
        assertEquals(map.get("10").getParents().size(), 1);
        assertEquals(map.get("10").getParents().get(0).getRequirement().getValue().getValue(), "0");
        assertEquals(map.get("10").getParents().get(0).getName(), "x1");

        assertEquals(map.get("01").getP(), 0.2, 0.05);
        assertEquals(map.get("01").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("01").getLocalRequirement().getValue().getAttributeName(), "x2");
        assertEquals(map.get("01").getParents().size(), 1);
        assertEquals(map.get("01").getParents().get(0).getRequirement().getValue().getValue(), "1");
        assertEquals(map.get("01").getParents().get(0).getName(), "x1");

        assertEquals(map.get("11").getP(), 0.8, 0.05);
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
        assertEquals(map.get("00").getP(), 0.8, 0.05);
        assertEquals(map.get("00").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("00").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("00").getParents().size(), 1);
        assertEquals(map.get("00").getParents().get(0).getRequirement().getValue().getValue(), "0");
        assertEquals(map.get("00").getParents().get(0).getName(), "x2");

        assertEquals(map.get("10").getP(), 0.2, 0.05);
        assertEquals(map.get("10").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("10").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("10").getParents().size(), 1);
        assertEquals(map.get("10").getParents().get(0).getRequirement().getValue().getValue(), "0");
        assertEquals(map.get("10").getParents().get(0).getName(), "x2");

        assertEquals(map.get("01").getP(), 0.001, 0.05);
        assertEquals(map.get("01").getLocalRequirement().getValue().getValue(), "0");
        assertEquals(map.get("01").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("01").getParents().size(), 1);
        assertEquals(map.get("01").getParents().get(0).getRequirement().getValue().getValue(), "1");
        assertEquals(map.get("01").getParents().get(0).getName(), "x2");

        assertEquals(map.get("11").getP(), 0.999, 0.05);
        assertEquals(map.get("11").getLocalRequirement().getValue().getValue(), "1");
        assertEquals(map.get("11").getLocalRequirement().getValue().getAttributeName(), "x3");
        assertEquals(map.get("11").getParents().size(), 1);
        assertEquals(map.get("11").getParents().get(0).getRequirement().getValue().getValue(), "1");
        assertEquals(map.get("11").getParents().get(0).getName(), "x2");

        //assert there are two sets of sliblings as there is 1 parent with 2 unique values
        //Set 1=2 and set 3=4 are supposed to be equal due to the starting node used
        sliblings1 = Node.findSliblings(nodes.get(2).getProbabilities().get(0), nodes.get(2));
        sliblings2 = Node.findSliblings(nodes.get(2).getProbabilities().get(1), nodes.get(2));
        sliblings3 = Node.findSliblings(nodes.get(2).getProbabilities().get(2), nodes.get(2));
        sliblings4 = Node.findSliblings(nodes.get(2).getProbabilities().get(3), nodes.get(2));
        assertTrue(sliblings1.containsAll(sliblings2));
        assertTrue(sliblings2.containsAll(sliblings1));
        assertFalse(sliblings2.containsAll(sliblings3));
        assertTrue(sliblings3.containsAll(sliblings4));
        assertTrue(sliblings4.containsAll(sliblings3));
        sliblings1.addAll(sliblings3);
        assertTrue(sliblings1.containsAll(nodes.get(2).getProbabilities()));
    }

    @Test
    public void testExpectionMaximizationAlarm3Parents() throws Exception {
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
        req.setTarget("VENTTUBE");

        List<Node> nodes = mapWebNodeToNode(central.expectationMaximization(req).getNodes());

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

    @Test
    public void testMaximumLikelyhoodAsia3Parties() throws Exception {
        //Small test with 3 parties.
        //1 node has no parents, so requires only 1 party
        //1 node has 1 parent, requires 2 parties
        //1 node has 2 parents, requires all 3 parties

        //Using maximumlikelyhood because the small network size means EM/synthetic generation fluctuates wildly
        // between runs, but maximumlikelyhood is stable, and the point of htis test is to test the federated bit

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

        List<WebNode> WebNodes = buildSmallAsiaNetwork();
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(WebNodes);
        req.setTarget("asia");

        VertiBayesCentralServer central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2, endpoint3), secretEnd);

        List<Node> nodes = mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

        // find the ventlung node, it's the one with three parents
        Node asia = null;
        for (Node n : nodes) {
            if (n.getName().equals("asia")) {
                asia = n;
                break;
            }
        }

        assertEquals(asia.getParents().size(), 0);
        assertEquals(asia.getUniquevalues().size(), 2);
        assertEquals(asia.getProbabilities().size(), 2);
        assertEquals(asia.getProbabilities().get(0).getP(), 0.98, 0.01);
        assertEquals(asia.getProbabilities().get(1).getP(), 0.02, 0.01);

        Node either = null;
        for (Node n : nodes) {
            if (n.getName().equals("either")) {
                either = n;
                break;
            }
        }

        assertEquals(either.getParents().size(), 1);
        assertEquals(either.getUniquevalues().size(), 2);
        assertEquals(either.getProbabilities().size(), 4);
        assertEquals(either.getProbabilities().get(0).getP(), 0.94, 0.01);
        assertEquals(either.getProbabilities().get(1).getP(), 0.06, 0.01);
        assertEquals(either.getProbabilities().get(2).getP(), 0.84, 0.01);
        assertEquals(either.getProbabilities().get(3).getP(), 0.16, 0.01);

        Node lung = null;
        for (Node n : nodes) {
            if (n.getName().equals("lung")) {
                lung = n;
                break;
            }
        }

        assertEquals(lung.getParents().size(), 2);
        assertEquals(lung.getUniquevalues().size(), 2);
        assertEquals(lung.getProbabilities().size(), 8);
        assertEquals(lung.getProbabilities().get(0).getP(), 0.999, 0.001);
        assertEquals(lung.getProbabilities().get(1).getP(), 0.001, 0.001);
        assertEquals(lung.getProbabilities().get(2).getP(), 0.13, 0.01);
        assertEquals(lung.getProbabilities().get(3).getP(), 0.87, 0.01);
        assertEquals(lung.getProbabilities().get(4).getP(), 0.999, 0.001);
        assertEquals(lung.getProbabilities().get(5).getP(), 0.001, 0.001);
        assertEquals(lung.getProbabilities().get(6).getP(), 0.36, 0.01);
        assertEquals(lung.getProbabilities().get(7).getP(), 0.64, 0.01);

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

    private List<WebNode> buildSmallAsiaNetwork() {
        WebNode asia = createWebNode("asia", Attribute.AttributeType.string, new ArrayList<>());
        WebNode either = createWebNode("either", Attribute.AttributeType.string, Arrays.asList(asia.getName()));
        WebNode lung = createWebNode("lung", Attribute.AttributeType.string,
                                     Arrays.asList(either.getName(), asia.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(asia, either, lung);
    }

    private WebNode createWebNode(String name, Attribute.AttributeType type, List<String> parents) {
        WebNode n = new WebNode();
        n.setType(type);
        n.setName(name);
        n.setParents(parents);
        return n;
    }
}