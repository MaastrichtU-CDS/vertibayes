package com.florian.vertibayes.util;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PgmxConverter {

    public static List<WebNode> fromPgmx(String bif) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(new InputSource(new StringReader(bif)));

        doc.getDocumentElement().normalize();

        NodeList root = ((NodeList) doc.getDocumentElement());
        NodeList probNet = findChildElement(root, "ProbNet");

        NodeList variables = findChildElement(probNet, "Variables");

        List<WebNode> nodes = new ArrayList<>();
        for (int i = 0; i < variables.getLength(); i++) {
            WebNode webNode = new WebNode();
            Node bifNode = variables.item(i);
            if (bifNode.getNodeType() == bifNode.ELEMENT_NODE) {
                String name = ((Element) bifNode).getAttribute("name");

                webNode.setName(name);
                nodes.add(webNode);
            }
        }
        return new ArrayList<>();

    }

    private static NodeList findChildElement(NodeList parent, String target) {
        for (int i = 0; i < parent.getLength(); i++) {
            Node n = parent.item(i);
            if (n.getNodeType() == n.ELEMENT_NODE) {
                if (n.getNodeName().equals(target)) {
                    return (NodeList) parent.item(i);
                }
            }
        }
        return null;
    }

    private static String getHeader() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ProbModelXML formatVersion=\"0.2.0\">\n" +
                "  <ProbNet type=\"InfluenceDiagram\">\n" +
                "    <DecisionCriteria>\n" +
                "      <Criterion name=\"---\" unit=\"---\" />\n" +
                "    </DecisionCriteria>\n" +
                "    <Properties />";
    }

    private static String getFooter() {
        return "</ProbModelXML>";
    }
}
