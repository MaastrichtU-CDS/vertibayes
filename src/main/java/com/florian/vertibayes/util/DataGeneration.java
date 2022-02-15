package com.florian.vertibayes.util;

import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirement;

import java.util.*;

import static com.florian.vertibayes.util.MathUtil.round;
import static com.florian.vertibayes.util.PrintingPress.printARFF;

public final class DataGeneration {

    public static final double MAX_VALUE = 0.99;

    private DataGeneration() {
    }

    public static void generateDataARRF(List<Node> nodes, int samplesize, String path) {
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

        printARFF(data, path);
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
                                    if (local.getLowerLimit().isUknown()) {
                                        individual.put(node.getName(), "?");
                                    } else {
                                        //generate a number from the range
                                        Double upper = Double.valueOf(local.getUpperLimit().getValue());
                                        Double lower = Double.valueOf(local.getLowerLimit().getValue());
                                        Double generated = random.nextDouble() * (upper - lower) + lower;
                                        if (round(generated, local.getLowerLimit().getType()) == upper) {
                                            // don't generate values equal to upperlimit
                                            generated = MAX_VALUE * upper;
                                        }
                                        individual.put(node.getName(),
                                                       String.valueOf(
                                                               round(generated, local.getLowerLimit().getType())));
                                    }
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
                                    Attribute.AttributeType type = null;
                                    if (parent.getRequirement().isRange()) {
                                        type = parent.getRequirement().getLowerLimit().getType();
                                    } else {
                                        type = parent.getRequirement().getValue().getType();
                                    }
                                    Attribute a = new Attribute(type,
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
                                        if (local.getLowerLimit().isUknown()) {
                                            individual.put(node.getName(), "?");
                                        } else {
                                            Double upper = Double.valueOf(local.getUpperLimit().getValue());
                                            Double lower = Double.valueOf(local.getLowerLimit().getValue());
                                            Double generated = random.nextDouble() * (upper - lower) + lower;
                                            if (round(generated, local.getLowerLimit().getType()) == upper) {
                                                // don't generate values equal to upperlimit
                                                generated = MAX_VALUE * upper;
                                            }
                                            individual.put(node.getName(), String.valueOf(
                                                    round(generated, local.getLowerLimit().getType())));
                                        }
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
}
