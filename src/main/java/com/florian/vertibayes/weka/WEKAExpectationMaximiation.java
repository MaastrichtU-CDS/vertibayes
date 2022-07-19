package com.florian.vertibayes.weka;

import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationTestResponse;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.estimate.SimpleEstimator;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.florian.vertibayes.util.DataGeneration.generateDataARRF;
import static com.florian.vertibayes.webservice.mapping.WebNodeMapper.mapWebNodeToNode;
import static com.florian.vertibayes.weka.BifMapper.fromBif;
import static com.florian.vertibayes.weka.BifMapper.toBIF;

public final class WEKAExpectationMaximiation {
    private static final String BIFF = "test.xml";
    private static final String ARFF = "test.arff";
    private static final int SAMPLE_SIZE = 10000;
    public static final int FOLDS = 10;

    private WEKAExpectationMaximiation() {
    }

    public static ExpectationMaximizationTestResponse wekaExpectationMaximization(List<WebNode> nodes,
                                                                                  String target)
            throws Exception {

        generateDataARRF(mapWebNodeToNode(nodes), SAMPLE_SIZE, ARFF);
        printBIF(toBIF(nodes));


        FromFile search = new FromFile();
        search.setBIFFile(BIFF);

        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = new Instances(
                new BufferedReader(new FileReader(ARFF)));

        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).name().equals(target)) {
                data.setClassIndex(i);
                break;
            }
        }

        network.setEstimator(new SimpleEstimator());

        network.buildClassifier(data);


        ExpectationMaximizationTestResponse response = new ExpectationMaximizationTestResponse();

        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(network, data, FOLDS, new Random(1));
        response.setSyntheticAuc(eval.weightedAreaUnderROC());

        response.setWeka(network);
        response.setNodes(fromBif(network.graph()));
        return response;
    }

    private static void printBIF(String bif) {
        File bifFile = new File(BIFF);
        List<String> data = Arrays.stream(bif.split("\n")).collect(Collectors.toList());
        try (PrintWriter pw = new PrintWriter(bifFile)) {
            data.stream()
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
