package com.florian.vertibayes.notunittests.generatedata;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.data.Data;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.webservice.mapping.WebNodeMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.florian.nscalarproduct.data.Parser.parseCsv;
import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildDiabetesNetwork;
import static com.florian.vertibayes.util.PrintingPress.printARFF;
import static com.florian.vertibayes.util.PrintingPress.printCSV;

public class DiscretizeData {
    private static final String CSV_PATH_IRIS_FULL = "resources/Experiments/iris/iris.csv";
    private static final String CSV_IRIS_TARGET = "resources/Experiments/IrisDiscrete/iris.csv";
    private static final String CSV_PATH_DIABETES_FULL = "resources/Experiments/diabetes/diabetes.csv";
    private static final String CSV_DIABETES_TARGET = "resources/Experiments/diabetesDiscrete/diabetes.csv";
    private List<Double> bins = Arrays.asList(0.1, 0.25, 0.4);

    @Test
    public void discretize() {
//        discretize(CSV_PATH_IRIS_FULL, CSV_PATH_IRIS_FULL, CSV_IRIS_TARGET, 0.1, buildIrisNetworkNoBins());
        discretize(CSV_PATH_DIABETES_FULL, CSV_PATH_DIABETES_FULL, CSV_DIABETES_TARGET, 0.1, buildDiabetesNetwork());
    }


    private void discretize(String centralPath, String toDiscretize, String target, double minPercentage,
                            List<WebNode> network) {
        String path = toDiscretize;

        String targetCSV = target;
        String targetArff = target.replace(".csv", ".arff");

        List<Node> nodes = WebNodeMapper.mapWebNodeToNode(network);
        setBins(centralPath, nodes, minPercentage);
        List<List<Attribute>> discreet = discretize(path, nodes);

        discretizeNetwork(nodes);

        printCSV(createCSV(discreet), targetCSV);
        printARFF(createArff(discreet, nodes), targetArff);
    }

    private void discretizeNetwork(List<Node> nodes) {
        for (Node node : nodes) {
            if (node.getType() == Attribute.AttributeType.real || node.getType() == Attribute.AttributeType.numeric) {
                node.setType(Attribute.AttributeType.string);
                node.setUniquevalues(new HashSet<>());
                for (Bin b : node.getBins()) {
                    String value = b.getLowerLimit() + "_" + b.getUpperLimit();
                    node.getUniquevalues().add(value);
                }
                node.setBins(null);
            }
        }
    }

    private List<String> createArff(List<List<Attribute>> data, List<Node> network) {
        List<String> d = new ArrayList<>();
        String s = "@Relation genericBIFF";
        d.add(s);

        for (Node n : network) {
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
            d.add(s);
        }
        d.add("");
        d.add("@DATA");

        for (int i = 0; i < data.get(0).size(); i++) {
            s = "";
            for (List<Attribute> a : data) {
                if (findNode(a.get(0).getAttributeName(), network) == null) {
                    //ID node, skip
                    continue;
                }
                if (s.length() >= 1) {
                    s += ",";
                }
                s += a.get(i).getValue();
            }
            d.add(s);
        }
        return d;
    }

    private List<String> createCSV(List<List<Attribute>> data) {
        List<String> d = new ArrayList<>();
        String s = "";

        String header = "";
        for (List<Attribute> a : data) {
            if (s.length() >= 1) {
                s += ",";
            }

            if (header.length() >= 1) {
                header += ",";
            }
            header += a.get(0).getType();

            s += a.get(0).getAttributeName();

        }
        d.add(header);
        d.add(s);

        for (int i = 0; i < data.get(0).size(); i++) {
            s = "";
            for (List<Attribute> a : data) {
                if (s.length() >= 1) {
                    s += ",";
                }
                s += a.get(i).getValue();
            }
            d.add(s);
        }

        return d;
    }

    private List<List<Attribute>> discretize(String path, List<Node> network) {
        Data d = parseCsv(path, 0);
        List<List<Attribute>> original = d.getData();
        List<List<Attribute>> copy = new ArrayList<>();
        for (int i = 0; i < original.size(); i++) {
            List<Attribute> list = original.get(i);
            Attribute.AttributeType type = list.get(0).getType();
            if (d.getIdColumn() == i || type != Attribute.AttributeType.numeric && type != Attribute.AttributeType.real) {
                copy.add(list);
            } else {
                List<Attribute> copies = new ArrayList<>();
                copy.add(copies);
                for (Attribute a : list) {
                    String name = a.getAttributeName();
                    Node node = findNode(name, network);

                    if (node.getBins() != null) {
                        for (Bin b : node.getBins()) {
                            Attribute lowerLimit = new Attribute(node.getType(), b.getLowerLimit(), node.getName());
                            Attribute upperLimit = new Attribute(node.getType(), b.getUpperLimit(), node.getName());
                            AttributeRequirement req = new AttributeRequirement(lowerLimit, upperLimit);
                            if (req.checkRequirement(a)) {
                                String value = b.getLowerLimit() + "_" + b.getUpperLimit();
                                Attribute c = new Attribute(Attribute.AttributeType.string, value, name);
                                copies.add(c);
                            }
                        }
                    }
                }
            }
        }
        return copy;
    }

    private void setBins(String path, List<Node> network, double minPercentage) {
        BayesServer server = new BayesServer(path, "1");
        for (Node node : network) {
            try {
                node.setBins(server.getBins(node.getName(), minPercentage));
            } catch (NumberFormatException e) {
                //conti
                node.setUniquevalues(server.getUniqueValues(node.getName()));
            }
        }
    }

    private static Node findNode(String name, List<Node> nodes) {
        for (Node n : nodes) {
            if (n.getName().equals(name)) {
                return n;
            }
        }
        return null;
    }


}
