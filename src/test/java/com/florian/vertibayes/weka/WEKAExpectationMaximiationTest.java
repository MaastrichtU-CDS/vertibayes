package com.florian.vertibayes.weka;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.webservice.mapping.WebNodeMapper;
import org.apache.commons.collections.map.HashedMap;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WEKAExpectationMaximiationTest {
    private static final String BIFF = "resources/Experiments/k2/k2.xml";
    private static final String ARFF = "resources/Experiments/k2/k2.arff";
    private static final String PATH = "test.arff";

    @Test
    public void testGenerateMissingValues() throws Exception {
        generateData();
        String[] data = retrieveData().split("\n");
        Map<Integer, Boolean> hasUnknown = new HashedMap();
        // three attributes, make map to keep track if all of them have at least 1 unknown value
        hasUnknown.put(0, false);
        hasUnknown.put(1, false);
        hasUnknown.put(2, false);
        for (int i = 1; i < data.length - 1; i++) {
            //Skip first and last row, those are leftovers from how the file is read
            String[] attributes = data[i].split(",");
            for (int j = 0; j < attributes.length; j++) {
                if (attributes[j].equals("?")) {
                    hasUnknown.put(j, true);
                }
            }
        }

        for (boolean unknown : hasUnknown.values()) {
            assertTrue(unknown);
        }
        String s = "";
    }

    private String retrieveData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(PATH));
        String lines = "";
        String line;

        while ((line = reader.readLine()) != null) {
            lines += line + "\n";
        }
        String[] split = lines.split("@DATA");
        return split[1];
    }

    private void generateData() throws Exception {
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
        List<WebNode> webNodes = central.buildNetwork().getNodes();
        //add simple bins"to all nodes expect X3. The bins will essentially just form the binary division again
        // Since the bins will follow the same distribution we know the expected probabilities
        Bin zero = new Bin();
        zero.setLowerLimit("-1");
        zero.setUpperLimit("0.5");

        Bin one = new Bin();
        one.setLowerLimit("0.5");
        one.setUpperLimit("1.5");

        Bin unknown = new Bin();
        unknown.setLowerLimit("?");
        unknown.setUpperLimit("?");
        for (WebNode node : webNodes) {
            if (!node.getName().equals("x3")) {
                node.getBins().add(zero);
                node.getBins().add(one);
                node.getBins().add(unknown);
                node.setDiscrete(false);
            }
        }
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(webNodes);
        req.setTarget("x3");

        List<Node> nodes = WebNodeMapper.mapWebNodeToNode(central.expectationMaximization(req).getNodes());
    }


}