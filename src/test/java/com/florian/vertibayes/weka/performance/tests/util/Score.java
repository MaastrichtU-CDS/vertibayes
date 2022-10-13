package com.florian.vertibayes.weka.performance.tests.util;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

import static com.florian.vertibayes.webservice.mapping.WebNodeMapper.mapWebNodeToNode;
import static com.florian.vertibayes.weka.BifMapper.fromWekaBif;

public class Score {

    public static double calculateAIC(Instances instances, BayesNet network) throws Exception {
        double score = 0;
        List<Node> nodes = mapWebNodeToNode(fromWekaBif(network.graph()));
        for (Node node : nodes) {
            for (Theta t : node.getProbabilities()) {
                double fP = t.getP();
                double count = countInstances(instances, t);
                score += (count * Math.log(fP));
            }
            score -= (node.getProbabilities().size() - 1);
        }
        return score;
    }

    private static double countInstances(Instances instances, Theta t) {
        int count = 0;
        for (int i = 0; i < instances.size(); i++) {
            boolean parentFailed = false;
            Instance instance = instances.get(i);
            int attributeIndex = instances.attribute(t.getLocalRequirement().getName()).index();
            Attribute a = createAttribute(t.getLocalRequirement(), instance.value(attributeIndex),
                                          instances.attribute(attributeIndex));
            if (!t.getLocalRequirement().checkRequirement(a)) {
                continue;
            }
            for (ParentValue p : t.getParents()) {
                int parentIndex = instances.attribute(p.getName()).index();
                Attribute parent = createAttribute(p.getRequirement(), instance.value(parentIndex),
                                                   instances.attribute(parentIndex));
                if (!p.getRequirement().checkRequirement(parent)) {
                    parentFailed = true;
                    break;
                }
            }
            if (!parentFailed) {
                count++;
            }
        }
        return count;
    }

    private static Attribute createAttribute(AttributeRequirement req, double value, weka.core.Attribute attribute) {
        String attributeValue = "";
        Attribute.AttributeType type = null;
        if (attribute.numValues() == 0) {
            attributeValue = String.valueOf(value);
        } else {
            attributeValue = attribute.value((int) value);
        }
        if (req.isRange()) {
            type = req.getLowerLimit().getType();
        } else {
            type = req.getValue().getType();
        }
        return new Attribute(type, attributeValue, req.getName());
    }
}
