package com.florian.vertibayes.weka.performance.tests.util;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.florian.vertibayes.util.DataGeneration.generateIndividual;
import static com.florian.vertibayes.util.PrintingPress.printARFF;
import static com.florian.vertibayes.webservice.mapping.WebNodeMapper.mapWebNodeToNode;

public class Util {
    private static double TEST_POPULATION = 10000;

    public static void generateSyntheticFold(BayesNet network, String testData, List<WebNode> original,
                                             List<WebNode> testNetwork, String target, double minPercentage,
                                             Performance per)
            throws Exception {
        
        VertiBayesCentralServer station = createCentral(testData, testData);
        WebBayesNetwork req = new WebBayesNetwork();
        req.setMinPercentage(minPercentage);
        req.setNodes(testNetwork);
        req.setTarget(target);
        WebBayesNetwork res = station.maximumLikelyhood(req);
        createArrf(original, res.getNodes(), 10000, "temp.arff");
        Instances test = readData(target, "temp.arff");
        Evaluation eval = new Evaluation(test);
        eval.evaluateModel(network, test);
        per.setSyntheticFoldAuc(eval.weightedAreaUnderROC());
    }

    public static Instances readData(String target, String arff) throws IOException {
        Instances data = new Instances(
                new BufferedReader(new FileReader(arff)));
        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).name().equals(target)) {
                data.setClassIndex(i);
                break;
            }
        }
        return data;
    }

    public static VertiBayesCentralServer createCentral(String firsthalf, String secondhalf) {
        BayesServer station1 = new BayesServer(firsthalf, "1");
        BayesServer station2 = new BayesServer(secondhalf, "2");

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

        VertiBayesCentralServer central = new VertiBayesCentralServer(true);
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2), secretEnd);
        return central;
    }

    public static List<Integer> recordErrors(BayesNet network, Instances testData)
            throws Exception {
        List<Integer> errors = new ArrayList<>();
        for (int i = 0; i < testData.size(); i++) {
            Instance inst = testData.get(i);
            if ((!Double.isNaN(inst.classValue())) && network.classifyInstance(inst) != inst.classValue()) {
                errors.add(i);
            }
        }

        return errors;
    }

    private static void createArrf(List<WebNode> original, List<WebNode> testNetwork, int samplesize, String path) {
        List<String> data = new ArrayList<>();
        //use the original to create the header so attribute values are set correctly
        //e.g. to deal with missing values in this one fold
        createHeader(mapWebNodeToNode(original), data);
        for (int i = 0; i < samplesize; i++) {
            data.add(generateIndividual(mapWebNodeToNode(testNetwork)));
        }
        printARFF(data, path);
    }

    private static void createHeader(List<Node> nodes, List<String> data) {
        String s = "@Relation genericBIFF";
        data.add(s);

        for (Node n : nodes) {
            s = "";
            s += "@Attribute";
            s += " " + n.getName() + " ";
            if (n.getType() == Attribute.AttributeType.string) {
                s += "{";
                int count = 0;
                for (String unique : n.getUniquevalues()) {
                    if (!unique.equals("?")) {
                        //only print valid values here, otherwiseweka will think ? is also valid.
                        if (count > 0) {
                            s += ",";
                        }
                        count++;
                        s += unique;
                    }
                }
                s += "}";
            } else {
                s += n.getType();
            }
            data.add(s);
        }
        data.add("");
        data.add("@DATA");
    }
}
