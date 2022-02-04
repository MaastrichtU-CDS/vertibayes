package com.florian.vertibayes.webservice.mapping;

import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.webservice.domain.external.WebParentValue;
import com.florian.vertibayes.webservice.domain.external.WebTheta;
import com.florian.vertibayes.webservice.domain.external.WebValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class WebNodeMapper {

    private WebNodeMapper() {
    }

    public static List<Node> mapWebNodeToNode(List<WebNode> input) {
        Map<String, Node> output = new HashMap<>();
        for (WebNode node : input) {
            Node n = new Node();
            n.setName(node.getName());
            n.setType(node.getType());
            if (node.getProbabilities() != null) {
                for (WebTheta theta : node.getProbabilities()) {
                    // if it is a discrete node, copy the local values, otherwise the bins are sufficient here
                    if (node.isDiscrete()) {
                        n.getUniquevalues().add(theta.getLocalValue().getLocalValue());
                    }
                }
            }
            n.setBins(node.getBins());
            output.put(n.getName(), n);

        }
        // map parents
        for (WebNode n : input) {
            setParents(n.getParents(), output.get(n.getName()), output);
        }
        // map probabilities
        for (WebNode node : input) {
            if (node.getProbabilities() != null) {
                mapToProbabilities(node.getProbabilities(), output.get(node.getName()), output);
            }
        }
        return output.values().stream().collect(Collectors.toList());
    }

    private static AttributeRequirement mapReqFromWebValue(Node node, WebValue v) {
        if (node.isDiscrete()) {
            return new AttributeRequirement(
                    new Attribute(node.getType(), v.getLocalValue(), node.getName()));
        } else {
            Attribute lowerLimit = new Attribute(node.getType(), v.getLowerLimit(),
                                                 node.getName());
            Attribute upperLimit = new Attribute(node.getType(), v.getUpperLimit(),
                                                 node.getName());
            return new AttributeRequirement(lowerLimit, upperLimit);
        }
    }

    private static void mapToProbabilities(List<WebTheta> probabilities, Node node, Map<String, Node> nodes) {
        List<Theta> prob = node.getProbabilities();
        for (WebTheta theta : probabilities) {
            Theta t = new Theta();
            t.setP(theta.getP());
            if (node.isDiscrete()) {
                t.setLocalRequirement(new AttributeRequirement(
                        new Attribute(node.getType(), theta.getLocalValue().getLocalValue(), node.getName())));
            }
            t.setLocalRequirement(mapReqFromWebValue(node, theta.getLocalValue()));
            List<ParentValue> parents = new ArrayList<>();

            List<WebParentValue> webParents = theta.getParentValues();
            if (webParents != null) {
                for (WebParentValue v : webParents) {
                    ParentValue p = new ParentValue();
                    Node parent = nodes.get(v.getParent());
                    p.setName(v.getParent());
                    p.setRequirement(mapReqFromWebValue(parent, v.getValue()));
                    parents.add(p);
                }
                t.setParents(parents);
            }
            prob.add(t);
        }
    }

    public static List<WebNode> mapWebNodeFromNode(List<Node> input) {
        List<WebNode> output = new ArrayList<>();
        for (Node node : input) {
            WebNode n = new WebNode();
            n.setName(node.getName());
            n.setType(node.getType());
            n.setParents(new ArrayList<>());
            n.setBins(node.getBins());
            for (Node parent : node.getParents()) {
                n.getParents().add(parent.getName());
            }
            n.setProbabilities(new ArrayList<>());
            for (Theta t : node.getProbabilities()) {
                WebTheta theta = new WebTheta();
                theta.setP(t.getP());
                WebValue value = new WebValue();
                if (n.isDiscrete()) {
                    value.setLocalValue(t.getLocalRequirement().getValue().getValue());
                    value.setRange(false);
                } else {
                    value.setLowerLimit(t.getLocalRequirement().getLowerLimit().getValue());
                    value.setUpperLimit(t.getLocalRequirement().getUpperLimit().getValue());
                    value.setRange(true);
                }
                theta.setLocalValue(value);
                theta.setParentValues(new ArrayList());
                for (ParentValue parent : t.getParents()) {
                    value = new WebValue();
                    if (!parent.getRequirement().isRange()) {
                        value.setLocalValue(parent.getRequirement().getValue().getValue());
                        value.setRange(false);
                    } else {
                        value.setLowerLimit(parent.getRequirement().getLowerLimit().getValue());
                        value.setUpperLimit(parent.getRequirement().getUpperLimit().getValue());
                        value.setRange(true);
                    }
                    theta.getParentValues().add(new WebParentValue(parent.getName(), value));
                }
                n.getProbabilities().add(theta);
            }
            output.add(n);
        }

        return output;
    }

    private static void setParents(List<String> parents, Node node, Map<String, Node> output) {
        for (String parent : parents) {
            node.getParents().add(output.get(parent));
        }
    }
}
