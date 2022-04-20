package com.florian.vertibayes.weka;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.webservice.domain.external.WebParentValue;
import com.florian.vertibayes.webservice.domain.external.WebTheta;
import com.florian.vertibayes.webservice.domain.external.WebValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BifMapper {
    // Utility class that can turn a WEKA bif into our network structure and vice-versa

    public static final int THREE = 3;

    private BifMapper() {
    }

    public static String toBIF(List<WebNode> nodes) {
        String bif = "";
        bif += HEADER;
        bif += NAME;
        String variables = "";
        String tables = "";

        for (WebNode node : nodes) {
            variables += createVariable(node);
            tables += createTables(node);
        }
        bif += variables;
        bif += tables;
        bif += FOOTER;
        return bif;
    }

    private static String createTables(WebNode node) {
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

    private static String createVariable(WebNode node) {
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

    public static List<WebNode> fromBif(String bif) {
        bif = bif.replace(HEADER, "").replace(FOOTER, "");

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

        WebNode n = findNode(name, nodes);
        boolean hasParents = false;

        if (split[1].contains("</GIVEN>")) {
            hasParents = true;
            split = split[1].split("<TABLE>");
            String[] parents = split[0].split("</GIVEN>");

            for (int i = 0; i < parents.length - 1; i++) {
                String parentName = parents[i].replace("<GIVEN>", "").replace("\n", "");
                n.getParents().add(parentName);
                WebNode parent = findNode(parentName, nodes);
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

    private static WebNode findNode(String name, List<WebNode> nodes) {
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

    private static final String NAME = "<NAME>genericBIFF-weka.filters.unsupervised.attribute" +
            ".ReplaceMissingValues</NAME>\n";

    private static final String HEADER = "<?xml version=\"1.0\"?>\n" +
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

    private static final String FOOTER = "</NETWORK>\n" + "</BIF>\n";
}
