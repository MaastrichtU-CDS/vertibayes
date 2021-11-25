package com.florian.vertibayes.notunittests.generatedata;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.MaximumLikelyhoodRequest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class GenerateData {
    private static final int POPULATION = 150;
    private static final String CSV_PATH = "output/generatedData.csv";
    public static final String FIRSTHALF = "resources/iris_firsthalf.csv";
    public static final String SECONDHALF = "resources/iris_secondhalf.csv";

    public List<Node> buildNetwork() {
        Node label = new Node();
        Node petallength = new Node();
        Node petalwidth = new Node();
        Node sepallength = new Node();
        Node sepalwidth = new Node();
        label.setName("label");
        label.setType(Attribute.AttributeType.string);
        petallength.setName("petallength");
        petallength.setType(Attribute.AttributeType.number);
        petalwidth.setName("petalwidth");
        petalwidth.setType(Attribute.AttributeType.number);
        sepallength.setName("sepallength");
        sepallength.setType(Attribute.AttributeType.number);
        sepalwidth.setName("sepalwidth");
        sepalwidth.setType(Attribute.AttributeType.number);
        petallength.getParents().add(label);
        petalwidth.getParents().add(label);
        petalwidth.getParents().add(petallength);
        sepalwidth.getParents().add(label);
        sepallength.getParents().add(label);
        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    @Test
    public void testGenerateData() {
        //utility function to generate data locally without needing to create an entire vantage6 setup
        //easier for experiments
        VertiBayesCentralServer central = createCentral();
        List<Node> nodes = buildNetwork();
        MaximumLikelyhoodRequest req = new MaximumLikelyhoodRequest();
        req.setNodes(nodes);
        central.maximumLikelyhood(req);

        //select a root
        Node root = null;
        for (Node n : nodes) {
            if (n.getParents().size() == 0) {
                root = n;
            }
        }
        //set child relationship
        for (Node n : nodes) {
            for (Node p : n.getParents()) {
                p.getChildren().add(n);
            }
        }
        List<String> data = new ArrayList<>();

        String types = "";
        String names = "";
        for (Node node : nodes) {
            types += node.getType();
            names += node.getName();
        }
        data.add(types);
        data.add(names);
        for (int i = 0; i < POPULATION; i++) {
            data.add(generateIndividual(nodes));
        }
        printCSV(data);
    }

    private void printCSV(List<String> data) {
        File csvOutputFile = new File(CSV_PATH);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            data.stream()
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String generateNames(Node node) {
        String s = node.getName().toString();
        if (node.getChildren().size() > 0) {
            for (Node n : node.getChildren()) {
                s += "," + generateNames(n);
            }
        }
        return s;
    }


    private String generateTypes(Node node) {
        String s = node.getType().toString();
        if (node.getChildren().size() > 0) {
            for (Node n : node.getChildren()) {
                s += "," + generateTypes(n);
            }
        }
        return s;
    }

    private String generateIndividual(List<Node> nodes) {
        Map<String, String> individual = new HashMap<>();
        boolean done = false;
        Random random = new Random();
        while (!done) {
            done = true;
            for (Node node : nodes) {
                if (individual.get(node.getName()) == null) {
                    done = false;
                    //this attribute does not have a value yet
                    double x = random.nextDouble();
                    double y = 0;
                    for (Theta theta : node.getProbabilities()) {
                        if (node.getParents().size() == 0) {
                            //no parents, just select a random value
                            y += theta.getP();
                            if (x <= y) {
                                individual.put(node.getName(), theta.getLocalValue().getValue());
                                break;
                            }
                        } else {
                            //node has parents, so check if parent values have been selected yet
                            boolean correctTheta = true;
                            for (ParentValue parent : theta.getParents()) {
                                if (individual.get(parent.getName()) == null) {
                                    //not all parents are selected, move on
                                    correctTheta = false;
                                    break;
                                } else if (individual.get(parent.getName()) != parent.getValue().getValue()) {
                                    //A parent has the wrong value, move on
                                    correctTheta = false;
                                    break;
                                }
                            }
                            if (correctTheta) {
                                y += theta.getP();
                                if (x <= y) {
                                    individual.put(node.getName(), theta.getLocalValue().getValue());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        String s = "";
        int i = 0;
        for (Node node : nodes) {
            if (i > 0) {
                s += ",";
            }
            i++;
            s += individual.get(node.getName());
        }

        return s;
    }

    private VertiBayesCentralServer createCentral() {
        BayesServer station1 = new BayesServer(FIRSTHALF, "1");
        BayesServer station2 = new BayesServer(SECONDHALF, "2");

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
        return central;
    }
}