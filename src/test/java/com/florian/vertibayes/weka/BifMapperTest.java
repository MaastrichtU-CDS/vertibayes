package com.florian.vertibayes.weka;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import org.junit.jupiter.api.Test;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildDiabetesNetwork;
import static com.florian.vertibayes.weka.BifMapper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BifMapperTest {
    private static final String BIFF = "resources/Experiments/k2/k2.xml";
    private static final String ARFF = "resources/Experiments/k2/k2.arff";

    private static final String OPENMARKOV_BIF = "resources/Experiments/model.pgmx";


    @Test
    public void testMapper() throws Exception {
        FromFile search = new FromFile();

        search.setBIFFile(BIFF);
        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = new Instances(
                new BufferedReader(new FileReader(ARFF)));
        data.setClassIndex(data.numAttributes() - 1);

        network.buildClassifier(data);

        String initialBif = network.graph();
        String mapped = toBIF(fromWekaBif(initialBif));

        assertEquals(initialBif, mapped);

    }

    @Test
    public void testMapperOpenMarkovBackAndForth() throws Exception {
        String bif = readFile("resources/Experiments/openMarkov.pgmx");
        List<WebNode> nodes = fromOpenMarkovBif(bif);
        String mapped = toOpenMarkovBif(nodes).replace("\r", "");

        assertEquals(bif, mapped);
    }

    @Test
    public void testMapperOpenMarkov() throws Exception {

        String expected = readFile(OPENMARKOV_BIF);

        List<WebNode> nodes = generateDiabetesNetwork();
        String mapped = toOpenMarkovBif(nodes);

        assertEquals(expected, mapped);

    }

    private String readFile(String path)
            throws IOException {
        File myObj = new File(path);
        String bif = "";
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            bif += data + "\n";
        }
        myReader.close();
        return bif;
    }

    private List<WebNode> generateDiabetesNetwork() throws Exception {
        //Small test with 3 parties.
        //1 node has no parents, so requires only 1 party
        //1 node has 1 parent, requires 2 parties
        //1 node has 2 parents, requires all 3 parties

        //Using maximumlikelyhood because the small network size means EM/synthetic generation fluctuates wildly
        // between runs, but maximumlikelyhood is stable, and the point of htis test is to test the federated bit

        BayesServer station1 = new BayesServer("resources/Experiments/diabetes/diabetes_firsthalf.csv", "1");
        BayesServer station2 = new BayesServer("resources/Experiments/diabetes/diabetes_secondhalf.csv", "2");

        VertiBayesEndpoint endpoint1 = new VertiBayesEndpoint(station1);
        VertiBayesEndpoint endpoint2 = new VertiBayesEndpoint(station2);
        BayesServer secret = new BayesServer("4", Arrays.asList(endpoint1, endpoint2));

        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpoint1);
        all.add(endpoint2);
        all.add(secretEnd);
        secret.setEndpoints(all);
        station1.setEndpoints(all);
        station2.setEndpoints(all);

        List<WebNode> WebNodes = buildDiabetesNetwork();
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(WebNodes);
        req.setTarget("Outcome");
        req.setFolds(3);

        VertiBayesCentralServer central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2), secretEnd);

        WebBayesNetwork res =
                central.maximumLikelyhood(
                        req);
        return res.getNodes();
    }

}