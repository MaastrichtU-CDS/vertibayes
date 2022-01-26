package com.florian.vertibayes.webservice.mapping;

import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.webservice.domain.WebNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebNodeMapper {
    public static List<Node> mapWebNodeToNode(List<WebNode> input) {
        Map<String, Node> output = new HashMap<>();
        for (WebNode node : input) {
            Node n = new Node();
            n.setName(node.getName());
            output.put(n.getName(), n);
        }
        for (WebNode n : input) {
            setParents(n.getParents(), output.get(n.getName()), output);
        }

        return output.values().stream().collect(Collectors.toList());
    }

    public static List<WebNode> mapWebNodeFromNode(List<Node> input) {
        List<WebNode> output = new ArrayList<>();
        for (Node node : input) {
            WebNode n = new WebNode();
            n.setName(node.getName());
            n.setType(node.getType());
            for (Node parent : node.getParents()) {
                n.getParents().add(parent.getName());
            }
        }

        return output;
    }

    private static void setParents(List<String> parents, Node node, Map<String, Node> output) {
        for (String parent : parents) {
            node.getParents().add(output.get(parent));
        }
    }
}
