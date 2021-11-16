package com.florian.vertibayes.util;

import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.bayes.data.Data;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.domain.AttributeRequirementsRequest;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BayesianNonsenseTest {
    Data data;

    @Test
    public void test() {
        BayesServer station1 = new BayesServer("resources/iris.csv", "1");
        List<Node> nodes = station1.createNodes().getNodes();
        int countZero = 0;
        int countOne = 0;
        Node label = null;
        for (Node node : nodes) {
            if (node.getName().equals("label")) {
                label = node;
            }
        }

        for (Node node : nodes) {
            if (node.getName().equals("label")) {
                continue;
            }
            node.getParents().add(label);
            label.getChildren().add(node);
        }

        for (Node node : nodes) {
            for (String unique : node.getUniquevalues()) {
                // generate base thetas
                Theta t = new Theta();
                t.setLocalValue(new Attribute(node.getType(), unique, node.getName()));
                node.getProbabilities().add(t);
            }
            for (Node parent : node.getParents()) {
                // for each parent
                List<Theta> copies = new ArrayList<>();
                for (String p : parent.getUniquevalues()) {
                    // for each parent value
                    ParentValue v = new ParentValue();
                    v.setName(parent.getName());
                    v.setValue(new Attribute(parent.getType(), p, parent.getName()));
                    for (Theta t : node.getProbabilities()) {
                        //Copy each current child, add the extra new parent
                        Theta copy = new Theta();
                        copy.setLocalValue(t.getLocalValue());

                        copy.setParents(new ArrayList<>());
                        copy.getParents().addAll(t.getParents());
                        copy.getParents().add(v);

                        copies.add(copy);
                    }
                }
                // remove old children, put in the new copies
                node.getProbabilities().removeAll(node.getProbabilities());
                node.getProbabilities().addAll(copies);
            }
            for (Theta t : node.getProbabilities()) {
                determineProb(station1, t);
            }
        }
        for (int i = 0; i < 150; i++) {
            System.out.println(generateIndividual(label, null));
        }
    }

    public String generateIndividual(Node node, Attribute parent) {
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
                        s += node.getName() + ":" + t.getLocalValue().getValue();
                        selected = t.getLocalValue();
                        break;
                    }
                }
            } else {
                y += t.getP();
                if (x <= y) {
                    s += node.getName() + ":" + t.getLocalValue().getValue();
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


    public void determineProb(BayesServer server, Theta t) {
        AttributeRequirementsRequest r = new AttributeRequirementsRequest();
        r.setRequirements(new ArrayList<>());

        if (t.getParents().size() > 0) {
            List<Attribute> parents = t.getParents().stream().map(x -> x.getValue()).collect(Collectors.toList());
            List<Attribute> list = new ArrayList<>();
            list.addAll(parents);
            list.add(t.getLocalValue());
            BigInteger count = countValue(list, server);
            BigInteger parentCount = countValue(parents, server);
            t.setP(count.doubleValue() / parentCount.doubleValue());
        } else {
            BigInteger count = countValue(Arrays.asList(t.getLocalValue()), server);
            t.setP(count.doubleValue() / (double) server.getPopulation());
        }
    }

    public BigInteger countValue(List<Attribute> a, BayesServer server) {
        AttributeRequirementsRequest r = new AttributeRequirementsRequest();
        r.setRequirements(new ArrayList<>());
        r.getRequirements().addAll(a);
        server.initK2Data(r);
        return server.count();
    }
}
