package com.florian.vertibayes.weka;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.florian.vertibayes.weka.BifMapper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BifMapperTest {
    private static final String BIFF = "resources/Experiments/k2/k2.xml";
    private static final String ARFF = "resources/Experiments/k2/k2.arff";

    private static final String OPENMARKOV_BIF = "resources/Experiments/model.pgmx";


    @Test
    public void testMapper() throws Exception {
        FromFile search = new FromFile();

        search.setBIFFile(BIFF);
        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = new Instances(
                new BufferedReader(new FileReader(ARFF)));
        data.setClassIndex(data.numAttributes() - 1);

        network.buildClassifier(data);

        String initialBif = network.graph();
        String mapped = toBIF(fromWekaBif(initialBif));

        assertEquals(initialBif, mapped);

    }

    @Test
    public void testMapperOpenMarkov() throws Exception {

        String bif = readFile(OPENMARKOV_BIF);
        Gson g = new Gson();
        List<WebNode> nodes = new ArrayList<>();
        nodes.add(g.fromJson(FIRST_NODE, WebNode.class));
        nodes.add(g.fromJson(SECOND_NODE, WebNode.class));
        nodes.add(g.fromJson(THIRD_NODE, WebNode.class));

        String mapped = toOpenMarkovBif(nodes);

        assertEquals(bif, mapped);

    }

    private String readFile(String path)
            throws IOException {
        File myObj = new File(path);
        String bif = "";
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            bif += data + "\n";
        }
        myReader.close();
        return bif;
    }

    private static final String FIRST_NODE = " {\n" +
            "            \"parents\" : [ ],\n" +
            "            \"name\" : \"x1\",\n" +
            "            \"type\" : \"numeric\",\n" +
            "            \"probabilities\" : [ ],\n" +
            "            \"bins\" : [ ]\n" +
            "            }";

    private static final String SECOND_NODE = "{\n" +
            "  \"parents\" : [ \"x1\" ],\n" +
            "  \"name\" : \"x2\",\n" +
            "  \"type\" : \"numeric\",\n" +
            "  \"probabilities\" : [ ],\n" +
            "  \"bins\" : [ ]\n" +
            "}";

    private static final String THIRD_NODE = "{\n" +
            "  \"parents\" : [ \"x2\" ],\n" +
            "  \"name\" : \"x3\",\n" +
            "  \"type\" : \"string\",\n" +
            "  \"probabilities\" : [ ],\n" +
            "  \"bins\" : [ ]\n" +
            "} ";


}