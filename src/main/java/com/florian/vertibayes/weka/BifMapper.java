package com.florian.vertibayes.weka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.webservice.domain.external.WebParentValue;
import com.florian.vertibayes.webservice.domain.external.WebTheta;
import com.florian.vertibayes.webservice.domain.external.WebValue;

import java.util.*;
import java.util.stream.Collectors;

import static com.florian.vertibayes.util.PrintingPress.prettyPrintByTransformer;
import static com.florian.vertibayes.webservice.mapping.WebNodeMapper.mapWebNodeFromNode;

public final class BifMapper {
    // Utility class that can turn a WEKA bif into our network structure and vice-versa

    public static final int THREE = 3;

    private BifMapper() {
    }

    public static List<WebNode> fromOpenMarkovBif(String bif) throws JsonProcessingException {
        bif = bif.replace(MARKOVHEADER, "");
        bif = bif.replace(MARKOVFOOTER, "");
        bif = bif.replace("\n", "").replace("\"", "").replace("/>", "");
        List<Node> nodes = new ArrayList<>();
        String[] split_1 = bif.split("<Links>");
        String[] variables = split_1[0].replace("<Variables>", "").replace("</Variables>", "").split("</Variable>");
        Map<String, List<String>> states = new HashMap<>();
        Map<String, List<Bin>> bins = new HashMap<>();
        nodes.addAll(createNodes(variables, states, bins));
        String[] split_2 = split_1[1].split("</Links>");
        String[] links = split_2[0].replace("<Link directed=true>", "").split("</Link>");
        linkParents(nodes, links);
        String[] potentials = split_2[1].replace("<Potentials>", "").replace("</Potentials>", "").split("</Potential>");

        setProbabilities(nodes, potentials, states, bins);

        return mapWebNodeFromNode(nodes);
    }

    private static void setProbabilities(List<Node> nodes, String[] potentials,
                                         Map<String, List<String>> states,
                                         Map<String, List<Bin>> bins) {
        for (String p : potentials) {
            String[] split = p.replace("</Values>", "").split("<Values>");
            split[0] = split[0].replace(" ", "")
                    .replace("<Potentialtype=Tablerole=conditionalProbability><Variables>", "");
            if (split[0].isEmpty()) {
                continue;
            }
            String[] variables = split[0].replace("</Variables>", "").split("<Variablename=");
            String[] probs = split[1].split(" ");
            Node child = findNode(variables[1], nodes);

            if (variables.length > 2) {
                List<Node> parents = new ArrayList<>();
                int count = 0;
                if (child.getType() == Attribute.AttributeType.string) {
                    count = states.get(child.getName()).size();
                } else {
                    count = bins.get(child.getName()).size();
                }
                for (int i = 2; i < variables.length; i++) {
                    parents.add(findNode(variables[i], nodes));
                }
                List<Integer> counts = new ArrayList<>();
                for (Node parent : parents) {
                    counts.add(-1);
                }
                for (int i = 0; i < probs.length; i++) {
                    Theta t = new Theta();
                    t.setP(Double.parseDouble(probs[i]));
                    createLocalRequirement(states, bins, child, i % count, t);
                    if (i % count == 0) {
                        for (int k = 0; k < counts.size(); k++) {
                            counts.set(k, counts.get(k) + 1);
                        }
                    }
                    for (int k = 0; k < counts.size(); k++) {
                        ParentValue pv = new ParentValue();
                        Node parent = parents.get(k);
                        pv.setName(parent.getName());
                        if (parent.getType() == Attribute.AttributeType.string) {
                            if (counts.get(k) == states.get(parent.getName()).size()) {
                                counts.set(k, 0);
                            }
                            pv.setRequirement(new AttributeRequirement(
                                    new Attribute(parent.getType(), states.get(parent.getName())
                                            .get(counts.get(k)), parent.getName())));
                        } else {
                            if (counts.get(k) == bins.get(parent.getName()).size()) {
                                counts.set(k, 0);
                            }
                            Attribute lower = new Attribute(parent.getType(), bins.get(parent.getName())
                                    .get(counts.get(k)).getLowerLimit(), parent.getName());
                            Attribute higher = new Attribute(parent.getType(), bins.get(parent.getName())
                                    .get(counts.get(k)).getUpperLimit(), parent.getName());
                            pv.setRequirement(new AttributeRequirement(lower, higher));
                        }
                        t.getParents().add(pv);
                    }
                    child.getProbabilities().add(t);
                }
            } else {
                for (int i = 0; i < probs.length; i++) {
                    Theta t = new Theta();
                    t.setP(Double.parseDouble(probs[i]));
                    createLocalRequirement(states, bins, child, i, t);
                    child.getProbabilities().add(t);
                }
            }
        }
    }

    private static void createLocalRequirement(Map<String, List<String>> states, Map<String, List<Bin>> bins,
                                               Node child, int i,
                                               Theta t) {
        if (child.getType() == Attribute.AttributeType.string) {
            Attribute req = new Attribute(child.getType(), states.get(child.getName()).get(i),
                                          child.getName());
            t.setLocalRequirement(new AttributeRequirement(req));
        } else {
            Attribute lower = new Attribute(child.getType(),
                                            bins.get(child.getName()).get(i).getLowerLimit(),
                                            child.getName());
            Attribute upper = new Attribute(child.getType(),
                                            bins.get(child.getName()).get(i).getUpperLimit(),
                                            child.getName());
            t.setLocalRequirement(new AttributeRequirement(lower, upper));
        }
    }

    private static void linkParents(List<Node> nodes, String[] links) {
        for (String l : links) {
            //take substring to skip the first symbol
            l = l.replace(" ", "").replace("<Variablename=", " ");
            if (l.isEmpty()) {
                continue;
            } else {
                l = l.substring(1);
            }
            String[] pairs = l.split(" ");
            Node parent = findNode(pairs[0], nodes);
            Node child = findNode(pairs[1], nodes);
            child.getParents().add(parent);
        }
    }

    private static List<Node> createNodes(String[] variables, Map<String, List<String>> uniqueValues,
                                          Map<String, List<Bin>> bins) {
        List<Node> nodes = new ArrayList<>();
        for (String v : variables) {
            List<Bin> stateBins = new ArrayList<>();
            List<String> unique = new ArrayList<>();
            v = v.replace(" ", "").split("<Thresholds>")[0];
            if (v.isEmpty()) {
                continue;
            }
            v.replace(" />", "");
            Node n = new Node();
            nodes.add(n);
            if (v.contains("discretized")) {
                n.setType(Attribute.AttributeType.real);
                String[] states = v.split("<States>")[1].replace("</States>", "").split("<Statename=");
                for (String s : states) {
                    if (s.isEmpty()) {
                        //first string is empty due to how splitting works
                        continue;
                    }
                    String[] values = s.replace("infinity", "inf").split(";");
                    Bin b = new Bin();
                    b.setLowerLimit(values[0]);
                    b.setUpperLimit(values[1]);
                    n.getBins().add(b);
                    if (!stateBins.contains(b)) {
                        stateBins.add(b);
                    }
                }
            } else {
                n.setType(Attribute.AttributeType.string);
                String[] states = v.split("<States>")[1].replace("</States>", "").split("<Statename=");
                for (String s : states) {
                    if (s.isEmpty()) {
                        continue;
                    }
                    n.getUniquevalues().add(s);
                    if (!unique.contains(s)) {
                        unique.add(s);
                    }
                }
            }
            String name = v.replace("<Variablename=", "").split("role=")[0];
            n.setName(name);
            if (n.getType() == Attribute.AttributeType.string) {
                uniqueValues.put(name, unique);
            } else {
                bins.put(name, stateBins);
            }
        }
        return nodes;

    }

    public static String toOpenMarkovBif(List<WebNode> nodes) {
        String bif = "";
        bif += WRITE_MARKOVHEADER;
        String variables = "";
        String potentials = "";
        String links = "";

        for (WebNode node : nodes) {
            variables += createMarkovVariable(node);
            links += createMarkovLink(node);
            potentials += createMarkovPotential(node);
        }
        bif += "<Variables>\n";
        bif += variables;
        bif += "</Variables>\n";
        bif += "<Links>\n";
        bif += links;
        bif += "</Links>\n";
        bif += "<Potentials>\n";
        bif += potentials;
        bif += "</Potentials>\n";
        bif += WRITE_MARKOVFOOTER;
        return prettyPrintByTransformer(bif.replace("\n", ""));
    }


    public static String toBIF(List<WebNode> nodes) {
        String bif = "";
        bif += WEKAHEADER;
        bif += NAME;
        String variables = "";
        String tables = "";

        for (WebNode node : nodes) {
            variables += createWekaVariable(node);
            tables += createWekaTables(node);
        }
        bif += variables;
        bif += tables;
        bif += WEKAFOOTER;
        return bif;
    }

    private static String createWekaTables(WebNode node) {
        String s = "";
        s += "<DEFINITION>\n";
        s += "<FOR>" + node.getName() + "</FOR>\n";
        for (String parent : node.getParents()) {
            s += "<GIVEN>" + parent + "</GIVEN>\n";
        }
        s += "<TABLE>\n";
        Set<String> unique = countUnique(node.getProbabilities());
        int count = 0;
        for (int i = 0; i < node.getProbabilities().size(); i++) {
            if (count > 0) {
                s += " ";
            }
            s += node.getProbabilities().get(i).getP();
            count++;
            if ((i + 1) % unique.size() == 0) {
                s += " \n";
                count = 0;
            }
        }
        s += "</TABLE>\n" + "</DEFINITION>\n";
        return s;

    }

    private static String createWekaVariable(WebNode node) {
        String s = "";
        s += "<VARIABLE TYPE=\"nature\">\n";
        s += "<NAME>" + node.getName() + "</NAME>\n";
        Set<String> unique = countUnique(node.getProbabilities());

        for (int i = 0; i < unique.size(); i++) {
            WebTheta t = node.getProbabilities().get(i);

            if (!t.getLocalValue().isRange()) {
                s += "<OUTCOME>" + t.getLocalValue().getLocalValue() + "</OUTCOME>\n";
            } else {
                s += "<OUTCOME>&apos;(";
                s += t.getLocalValue().getLowerLimit() + "-" + t.getLocalValue().getUpperLimit();
                if (i < unique.size() - 1) {
                    s += "]&apos;</OUTCOME>\n";
                } else {
                    s += ")&apos;</OUTCOME>\n";
                }
            }
        }

        s += "</VARIABLE>\n";
        return s;
    }


    private static String createMarkovPotential(WebNode node) {
        String s = "<Potential type=\"Table\" role=\"conditionalProbability\">\n";
        s += "<Variables>\n";
        s += "<Variable name=\"" + node.getName() + "\"/>\n";
        for (String parent : node.getParents()) {
            s += "<Variable name=\"" + parent + "\"/>\n";
        }
        s += "</Variables>\n";
        s += "<Values>";
        for (int i = 0; i < node.getProbabilities().size(); i++) {
            s += node.getProbabilities().get(i).getP();
            if (i != node.getProbabilities().size() - 1) {
                s += " ";
            }
        }
        s += "</Values>\n";
        s += "</Potential>\n";

        return s;

    }

    private static String createMarkovVariable(WebNode node) {
        String s = "";
        String treshholds = "";
        String type = "";
        boolean isNumeric = false;
        if (node.getType() == Attribute.AttributeType.string || node.getType() == Attribute.AttributeType.bool) {
            type = "\" type=\"finiteStates\"";
        } else {
            type = "\" type=\"discretized\"";
            treshholds = createTreshHolds(node);
            isNumeric = true;
        }
        s += "<Variable name=\"" + node.getName() + type + " role=\"chance\">\n";
        if (isNumeric) {
            s += "<Unit/>\n";
            s += "<Precision>0.01</Precision>\n";
        }
        s += "<States>\n";
        s += addMarkovStates(node);
        s += "</States>\n";
        s += treshholds;
        s += "</Variable>\n";
        return s;
    }

    private static String createTreshHolds(WebNode node) {
        String header = "<Thresholds>\n";
        String footer = "</Thresholds>\n";
        Set<String> tresholds = new LinkedHashSet<>();
        Set<String> unique = countUnique(node.getProbabilities());
        node.getProbabilities();
        boolean minus_inf = false;
        boolean inf = false;
        for (int i = 0; i < unique.size(); i++) {
            WebTheta t = node.getProbabilities().get(i);
            if (t.getLocalValue().getLowerLimit().equals("-inf")) {
                tresholds.add("-Infinity");
            }
            if (t.getLocalValue().getUpperLimit().equals("inf")) {
                tresholds.add("Infinity");
            }
            if (!t.getLocalValue().getLowerLimit().equals("-inf")) {
                tresholds.add(t.getLocalValue().getLowerLimit());
            }
            if (!t.getLocalValue().getUpperLimit().equals("inf")) {
                tresholds.add(t.getLocalValue().getUpperLimit());
            }
        }
        String s = "";
        for (String d : tresholds) {
            if (s.length() == 0) {
                s += header;
                s += "<Threshold value=\"" + d + "\" belongsTo=\"left\"/>\n";
            } else {
                s += "<Threshold value=\"" + d + "\" belongsTo=\"right\"/>\n";
            }
        }
        s += footer;
        return s;
    }

    private static String addMarkovStates(WebNode node) {
        String s = "";
        List<String> unique = countUnique(node.getProbabilities()).stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < unique.size(); i++) {
            WebTheta t = node.getProbabilities().get(i);
            if (!t.getLocalValue().isRange()) {
                s += "<State name=\"" + t.getLocalValue().getLocalValue() + "\"/>\n";
            } else {
                s += "<State name=\"" + t.getLocalValue().getLowerLimit() + ";" + t.getLocalValue()
                        .getUpperLimit() + "\"/>\n";
            }
        }
        return s;
    }

    private static String createMarkovLink(WebNode node) {

        if (node.getParents().size() == 0) {
            return "";
        }
        String s = "";
        for (String parent : node.getParents()) {
            s += "<Link directed=\"true\">\n";
            s += "<Variable name=\"" + parent + "\"/>\n";
            s += "<Variable name=\"" + node.getName() + "\"/>\n";
            s += "</Link>\n";
        }
        return s;
    }


    private static Set<String> countUnique(List<WebTheta> probabilites) {
        Set<String> unique = new HashSet<>();
        for (WebTheta t : probabilites) {
            if (!t.getLocalValue().isRange()) {
                unique.add(t.getLocalValue().getLocalValue());
            } else {
                unique.add(t.getLocalValue().getLowerLimit());
            }
        }
        return unique;
    }

    public static List<WebNode> fromWekaBif(String bif) {
        bif = bif.replace(WEKAHEADER, "").replace(WEKAFOOTER, "");

        String[] split = bif.split("</NAME>", 2)[1].split("</VARIABLE>");
        String tableString = split[split.length - 1];
        String[] tables = tableString.split("</DEFINITION>");

        String[] variables = new String[split.length - 1];
        for (int i = 0; i < split.length - 1; i++) {
            variables[i] = split[i];
        }
        List<WebNode> nodes = new ArrayList<>();
        for (String v : variables) {
            nodes.add(mapVariable(v));
        }

        for (int i = 0; i < tables.length - 1; i++) {
            mapTable(tables[i], nodes);
        }

        return nodes;
    }

    private static void mapTable(String table, List<WebNode> nodes) {
        table = table.replace("<DEFINITION>", "");
        String[] split = table.split("</FOR>");
        String name = split[0].replace("<FOR>", "").replace("\n", "");

        WebNode n = findWebNode(name, nodes);
        boolean hasParents = false;

        if (split[1].contains("</GIVEN>")) {
            hasParents = true;
            split = split[1].split("<TABLE>");
            String[] parents = split[0].split("</GIVEN>");

            for (int i = 0; i < parents.length - 1; i++) {
                String parentName = parents[i].replace("<GIVEN>", "").replace("\n", "");
                n.getParents().add(parentName);
                WebNode parent = findWebNode(parentName, nodes);
                List<WebTheta> copies = new ArrayList<>();
                Set<String> count = countUnique(parent.getProbabilities());
                for (int j = 0; j < count.size(); j++) {
                    WebTheta t = parent.getProbabilities().get(j);
                    for (WebTheta child : n.getProbabilities()) {
                        //Copy each current child, add the extra new parent
                        WebTheta copy = new WebTheta();
                        copy.setLocalValue(child.getLocalValue());
                        copy.getParentValues().addAll(child.getParentValues());
                        copy.getParentValues().add(new WebParentValue(parentName, t.getLocalValue()));
                        copies.add(copy);
                    }
                }
                n.setProbabilities(copies);
            }
        }

        String prob = "";
        if (hasParents) {
            prob = split[1].replace("\n", "").replace("<TABLE>", "").replace("</TABLE>", "");
        } else {
            prob = split[1].replace("\n", "").replace("<TABLE>", "").replace("</TABLE>", "");
        }
        String[] probabilities = prob.split(" ");

        for (int i = 0; i < probabilities.length; i++) {
            try {
                n.getProbabilities().get(i).setP(Double.parseDouble(probabilities[i]));
            } catch (Exception e) {
                System.out.println("");
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


    private static WebNode findWebNode(String name, List<WebNode> nodes) {
        for (WebNode n : nodes) {
            if (n.getName().equals(name)) {
                return n;
            }
        }
        return null;
    }

    private static WebNode mapVariable(String variable) {
        variable = variable.replace("<VARIABLE TYPE=\"nature\">", "");
        variable = variable.replace("<NAME>", "");
        String[] split = variable.split("</NAME>");
        WebNode n = new WebNode();
        n.setName(split[0].replace("\n", ""));

        for (int i = 1; i < split.length; i++) {
            String[] outcomes = split[i].split("</OUTCOME>");
            for (int j = 0; j < outcomes.length - 1; j++) {
                String outcome = outcomes[j].replace("<OUTCOME>", "");
                if (outcome.contains("&apos;")) {
                    // attribute is a range
                    Bin bin = new Bin();
                    if (outcome.contains(".")) {
                        //attribute is a double
                        n.setType(Attribute.AttributeType.real);
                    } else {
                        //attribute is an int
                        n.setType(Attribute.AttributeType.numeric);
                    }
                    String[] limits = outcome.replace("&apos;", "").replace("(", "").replace(")", "").replace("]", "")
                            .split("-");
                    String lower = "";
                    if (limits[0].equals("\n")) {
                        //first limit is empty, indicates the lowr limit was negative
                        lower += "-" + limits[1];
                    } else {
                        lower = limits[0].replace("\n", "");
                    }
                    bin.setLowerLimit(lower);
                    String upper = "";
                    if (limits.length == 1) {
                        //this only happens if it's concluded that all values fall within range
                        upper = lower;
                    } else if (limits.length == 2) {
                        upper = limits[1];
                    } else if (limits[2].equals("\n")) {
                        upper += "-" + limits[THREE];
                    } else {
                        upper = limits[2];
                    }
                    bin.setUpperLimit(upper);
                    n.getBins().add(bin);
                    WebValue v = new WebValue();
                    v.setRange(true);
                    v.setLowerLimit(lower);
                    v.setUpperLimit(upper);
                    WebTheta t = new WebTheta();
                    t.setLocalValue(v);
                    n.getProbabilities().add(t);
                } else {
                    //Attribute is a string
                    n.setType(Attribute.AttributeType.string);
                    WebValue v = new WebValue();
                    v.setLocalValue(outcome.replace("\n", ""));
                    v.setRange(false);
                    WebTheta t = new WebTheta();
                    t.setLocalValue(v);
                    n.getProbabilities().add(t);
                }
            }
        }
        return n;
    }

    private static final String WRITE_MARKOVHEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<ProbModelXML formatVersion=\"0.2.0\">\n" +
            "<ProbNet type=\"BayesianNetwork\">\n" +
            "<DecisionCriteria>\n" +
            "<Criterion name=\"---\" unit=\"---\"/>\n" +
            "</DecisionCriteria>\n" +
            "<Properties/>\n";
    private static final String MARKOVHEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<ProbModelXML formatVersion=\"0.2.0\">\n" +
            "  <ProbNet type=\"BayesianNetwork\">\n" +
            "    <DecisionCriteria>\n" +
            "      <Criterion name=\"---\" unit=\"---\"/>\n" +
            "    </DecisionCriteria>\n" +
            "    <Properties/>\n";


    private static final String MARKOVFOOTER = "  </ProbNet>\n" +
            "  <InferenceOptions>\n" +
            "    <MulticriteriaOptions>\n" +
            "      <SelectedAnalysisType>UNICRITERION</SelectedAnalysisType>\n" +
            "      <Unicriterion>\n" +
            "        <Scales>\n" +
            "          <Scale Criterion=\"---\" Value=\"1.0\"/>\n" +
            "        </Scales>\n" +
            "      </Unicriterion>\n" +
            "      <CostEffectiveness>\n" +
            "        <Scales>\n" +
            "          <Scale Criterion=\"---\" Value=\"1.0\"/>\n" +
            "        </Scales>\n" +
            "        <CE_Criteria>\n" +
            "          <CE_Criterion Criterion=\"---\" Value=\"Cost\"/>\n" +
            "        </CE_Criteria>\n" +
            "      </CostEffectiveness>\n" +
            "    </MulticriteriaOptions>\n" +
            "  </InferenceOptions>\n" +
            "</ProbModelXML>\n";

    private static final String WRITE_MARKOVFOOTER = "</ProbNet>\n" +
            "<InferenceOptions>\n" +
            "<MulticriteriaOptions>\n" +
            "<SelectedAnalysisType>UNICRITERION</SelectedAnalysisType>\n" +
            "<Unicriterion>\n" +
            "<Scales>\n" +
            "<Scale Criterion=\"---\" Value=\"1.0\"/>\n" +
            "</Scales>\n" +
            "</Unicriterion>\n" +
            "<CostEffectiveness>\n" +
            "<Scales>\n" +
            "<Scale Criterion=\"---\" Value=\"1.0\"/>\n" +
            "</Scales>\n" +
            "<CE_Criteria>\n" +
            "<CE_Criterion Criterion=\"---\" Value=\"Cost\"/>\n" +
            "</CE_Criteria>\n" +
            "</CostEffectiveness>\n" +
            "</MulticriteriaOptions>\n" +
            "</InferenceOptions>\n" +
            "</ProbModelXML>\n";


    private static final String NAME = "<NAME>genericBIFF-weka.filters.unsupervised.attribute" +
            ".ReplaceMissingValues</NAME>\n";

    private static final String WEKAHEADER = "<?xml version=\"1.0\"?>\n" +
            "<!-- DTD for the XMLBIF 0.3 format -->\n" +
            "<!DOCTYPE BIF [\n" +
            "\t<!ELEMENT BIF ( NETWORK )*>\n" +
            "\t      <!ATTLIST BIF VERSION CDATA #REQUIRED>\n" +
            "\t<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n" +
            "\t<!ELEMENT NAME (#PCDATA)>\n" +
            "\t<!ELEMENT VARIABLE ( NAME, ( OUTCOME |  PROPERTY )* ) >\n" +
            "\t      <!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n" +
            "\t<!ELEMENT OUTCOME (#PCDATA)>\n" +
            "\t<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n" +
            "\t<!ELEMENT FOR (#PCDATA)>\n" +
            "\t<!ELEMENT GIVEN (#PCDATA)>\n" +
            "\t<!ELEMENT TABLE (#PCDATA)>\n" +
            "\t<!ELEMENT PROPERTY (#PCDATA)>\n" +
            "]>\n" +
            "\n" +
            "\n" +
            "<BIF VERSION=\"0.3\">\n" +
            "<NETWORK>\n";

    private static final String WEKAFOOTER = "</NETWORK>\n" + "</BIF>\n";
}
