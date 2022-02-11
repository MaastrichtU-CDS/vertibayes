package com.florian.vertibayes.weka;

import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.estimate.SimpleEstimator;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.florian.vertibayes.webservice.mapping.WebNodeMapper.mapWebNodeToNode;
import static com.florian.vertibayes.weka.BifMapper.fromBif;
import static com.florian.vertibayes.weka.BifMapper.toBIF;

public final class WEKAExpectationMaximiation {
    private static final String BIFF = "test.xml";
    private static final String ARFF = "test.arff";
    public static final String weka = "resources/Experiments/iris/irisWeka2.csv";
    public static final String wekaArff = "resources/Experiments/iris/irisWeka2.arff";

    private WEKAExpectationMaximiation() {
    }

    public static List<WebNode> wekaExpectationMaximization(List<WebNode> nodes, int sampleSize, String target)
            throws Exception {

        generateData(mapWebNodeToNode(nodes), 150);
        generateDataCSV("outputOld.csv", 150, mapWebNodeToNode(nodes));
        printBIF(toBIF(nodes));


        FromFile search = new FromFile();
        search.setBIFFile(BIFF);

        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = new Instances(
                new BufferedReader(new FileReader(ARFF)));

        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).name().equals(target)) {
                data.setClassIndex(i);
                break;
            }
        }

        Instances test = new Instances(
                new BufferedReader(new FileReader(ARFF)));

        for (int i = 0; i < test.numAttributes(); i++) {
            if (test.attribute(i).name().equals(target)) {
                test.setClassIndex(i);
                break;
            }
        }

        network.setEstimator(new SimpleEstimator());

        network.buildClassifier(data);
        Evaluation eval = new Evaluation(test);
        eval.evaluateModel(network, test);
        eval.incorrect();

        return fromBif(network.graph());
    }

    private static void generateDataCSV(String output, int samplesize, List<Node> nodes) {
        List<String> data = new ArrayList<>();

        String names = "";
        int j = 0;
        for (Node node : nodes) {
            if (j > 0) {
                names += ",";
            }
            j++;
            names += node.getName();
        }
        data.add(names);
        for (int i = 0; i < samplesize; i++) {
            data.add(generateIndividualOld(nodes));
        }
        printCSV(data, output);
    }

    private static void printCSV(List<String> data, String path) {
        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            data.stream()
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private static String generateIndividualOld(List<Node> nodes) {
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
                                AttributeRequirement local = theta.getLocalRequirement();
                                if (!local.isRange()) {
                                    individual.put(node.getName(), local.getValue().getValue());
                                } else {
                                    //generate a number from the range
                                    Double upper = Double.valueOf(local.getUpperLimit().getValue());
                                    Double lower = Double.valueOf(local.getLowerLimit().getValue());
                                    Double generated = random.nextDouble() * (upper - lower) + lower;
                                    individual.put(node.getName(),
                                                   String.valueOf(round(generated, local.getLowerLimit().getType())));
                                }
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
                                } else {
                                    Attribute a = new Attribute(parent.getRequirement().getValue().getType(),
                                                                individual.get(parent.getName()), parent.getName());
                                    if (!parent.getRequirement().checkRequirement(
                                            a)) {
                                        //A parent has the wrong value for this theta, move on
                                        correctTheta = false;
                                        break;
                                    }

                                }
                            }
                            if (correctTheta) {
                                y += theta.getP();
                                if (x <= y) {
                                    AttributeRequirement local = theta.getLocalRequirement();
                                    if (!local.isRange()) {
                                        individual.put(node.getName(), local.getValue().getValue());
                                    } else {
                                        //generate a number from the range
                                        Double upper = Double.valueOf(local.getUpperLimit().getValue());
                                        Double lower = Double.valueOf(local.getLowerLimit().getValue());
                                        Double generated = random.nextDouble() * (upper - lower) + lower;
                                        individual.put(node.getName(), String.valueOf(
                                                round(generated, local.getLowerLimit().getType())));
                                    }
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


    private static void generateData(List<Node> nodes, int samplesize) {
        List<String> data = new ArrayList<>();
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

        for (int i = 0; i < samplesize; i++) {
            data.add(generateIndividual(nodes));
        }

        printARFF(data);
    }

    private static String generateIndividual(List<Node> nodes) {
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
                                AttributeRequirement local = theta.getLocalRequirement();
                                if (!local.isRange()) {
                                    individual.put(node.getName(), local.getValue().getValue());
                                } else {
                                    //generate a number from the range
                                    Double upper = Double.valueOf(local.getUpperLimit().getValue());
                                    Double lower = Double.valueOf(local.getLowerLimit().getValue());
                                    Double generated = random.nextDouble() * (upper - lower) + lower;
                                    individual.put(node.getName(),
                                                   String.valueOf(round(generated, local.getLowerLimit().getType())));
                                }
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
                                } else {
                                    Attribute a = new Attribute(parent.getRequirement().getValue().getType(),
                                                                individual.get(parent.getName()), parent.getName());
                                    if (!parent.getRequirement().checkRequirement(
                                            a)) {
                                        //A parent has the wrong value for this theta, move on
                                        correctTheta = false;
                                        break;
                                    }

                                }
                            }
                            if (correctTheta) {
                                y += theta.getP();
                                if (x <= y) {
                                    AttributeRequirement local = theta.getLocalRequirement();
                                    if (!local.isRange()) {
                                        individual.put(node.getName(), local.getValue().getValue());
                                    } else {
                                        //generate a number from the range
                                        Double upper = Double.valueOf(local.getUpperLimit().getValue());
                                        Double lower = Double.valueOf(local.getLowerLimit().getValue());
                                        Double generated = random.nextDouble() * (upper - lower) + lower;
                                        individual.put(node.getName(), String.valueOf(
                                                round(generated, local.getLowerLimit().getType())));
                                    }
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

    private static void printBIF(String bif) {
        File bifFile = new File(BIFF);
        List<String> data = Arrays.stream(bif.split("\n")).collect(Collectors.toList());
        try (PrintWriter pw = new PrintWriter(bifFile)) {
            data.stream()
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private static void printARFF(List<String> data) {
        File csvOutputFile = new File(ARFF);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            data.stream()
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static double round(double value, Attribute.AttributeType type) {
        int decimals = type == Attribute.AttributeType.numeric ? 0 : 3;
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
