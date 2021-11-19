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
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GenerateData {
    private static final int POPULATION = 150;
    private static final String CSV_PATH = "output/generatedData.csv";

    @Test
    @Ignore
    public void generateData() {
        //utility function to generate data locally without needing to create an entire vantage6 setup
        //easier for experiments
        BayesServer station1 = new BayesServer("resources/smallK2Example_firsthalf.csv", "1");
        BayesServer station2 = new BayesServer("resources/smallK2Example_secondhalf.csv", "2");

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

        String types = "string," + generateTypes(root);
        String names = "ID," + generateNames(root);
        data.add(types);
        data.add(names);
        for (int i = 0; i < POPULATION; i++) {
            String individual = i + "," + generateIndividual(root, null);
            data.add(individual);
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

    private String generateIndividual(Node node, Attribute parent) {
        Random r = new Random();
        double x = r.nextDouble();
        double y = 0;
        String s = "";
        Attribute selected = null;
        for (Theta t : node.getProbabilities()) {
            if (parent != null) {
                ParentValue p = t.getParents().get(0);
                if (p.getValue().equals(parent)) {
                    y += t.getP();
                    if (x <= y) {
                        s += t.getLocalValue().getValue();
                        selected = t.getLocalValue();
                        break;
                    }
                }
            } else {
                y += t.getP();
                if (x <= y) {
                    s += t.getLocalValue().getValue();
                    selected = t.getLocalValue();
                    break;
                }
            }
        }
        if (node.getChildren().size() > 0) {
            for (Node n : node.getChildren()) {
                s += "," + generateIndividual(n, selected);
            }
        }
        return s;
    }
}