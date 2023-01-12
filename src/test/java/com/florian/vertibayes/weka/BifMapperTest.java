package com.florian.vertibayes.weka;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import org.junit.jupiter.api.Test;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public void testMapperOpenMarkovBackAndForth() throws Exception {
        String bif = readFile("resources/Experiments/openMarkov.pgmx");
        List<WebNode> nodes = fromOpenMarkovBif(bif);
        String mapped = toOpenMarkovBif(nodes).replace("\r", "");

        assertEquals(bif, mapped);
    }

    @Test
    public void testMapperOpenMarkovBackAndForth2() throws Exception {
        String bif = readFile("resources/Experiments/openMarkov2.pgmx");
        List<WebNode> nodes = fromOpenMarkovBif(bif);
        String mapped = toOpenMarkovBif(nodes).replace("\r", "");

        assertEquals(bif, mapped);
    }

    @Test
    public void testMapperOpenMarkovBackAndForth4() throws Exception {
        String bif = readFile("resources/Experiments/openMarkov4.pgmx");
        List<WebNode> nodes = fromOpenMarkovBif(bif);
        String mapped = toOpenMarkovBif(nodes).replace("\r", "");

        assertEquals(bif, mapped);
    }

    @Test
    public void testMapperOpenMarkovBackAndForth3() throws Exception {
        String bif = readFile("resources/Experiments/openMarkov3.pgmx");
        List<WebNode> nodes = fromOpenMarkovBif(bif);
        String mapped = toOpenMarkovBif(nodes).replace("\r", "");

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

}