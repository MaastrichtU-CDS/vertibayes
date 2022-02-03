package com.florian.vertibayes.weka;

import org.junit.jupiter.api.Test;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;

import static com.florian.vertibayes.weka.BifMapper.fromBif;
import static com.florian.vertibayes.weka.BifMapper.toBIF;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BifMapperTest {
    private static final String BIFF = "resources/Experiments/k2/k2.xml";
    private static final String ARFF = "resources/Experiments/k2/k2.arff";

    @Test
    public void testMapping() throws Exception {
        FromFile search = new FromFile();

        search.setBIFFile(BIFF);
        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = new Instances(
                new BufferedReader(new FileReader(ARFF)));
        data.setClassIndex(data.numAttributes() - 1);

        network.buildClassifier(data);

        String initialBif = network.graph();
        String mapped = toBIF(fromBif(initialBif));

        assertEquals(initialBif, mapped);
    }

}