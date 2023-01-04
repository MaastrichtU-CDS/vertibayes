package com.florian.vertibayes.weka;

import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationWekaResponse;
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

import static com.florian.vertibayes.util.DataGeneration.generateDataARRFString;
import static com.florian.vertibayes.webservice.mapping.WebNodeMapper.mapWebNodeToNode;
import static com.florian.vertibayes.weka.BifMapper.fromWekaBif;
import static com.florian.vertibayes.weka.BifMapper.toBIF;

public final class WEKAExpectationMaximiation {
    private static final String BIFF = "test.xml";
    private static final int SAMPLE_SIZE = 10000;
    public static final int FOLDS = 10;

    private WEKAExpectationMaximiation() {
    }

    public static double validate(BayesNet trainModel, List<WebNode> validationModel, String target)
            throws Exception {
        String arff = generateDataARRFString(mapWebNodeToNode(validationModel), SAMPLE_SIZE);
        Instances data = new Instances(
                new BufferedReader(new StringReader(arff)));
        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).name().equals(target)) {
                data.setClassIndex(i);
                break;
            }
        }

        Evaluation eval = new Evaluation(data);
        eval.evaluateModel(trainModel, data);

        return eval.weightedAreaUnderROC();
    }

    public static ExpectationMaximizationWekaResponse wekaExpectationMaximization(List<WebNode> nodes,
                                                                                  String target)
            throws Exception {

        String arff = generateDataARRFString(mapWebNodeToNode(nodes), SAMPLE_SIZE);

        String unique = "";
        boolean isAvailable = false;
        int index = 0;
        while (!isAvailable) {
            unique = BIFF.replace(".xml", index + ".xml");
            File f = new File(unique);
            isAvailable = !f.exists();
            index += 1;
        }
        printBIF(toBIF(nodes), unique);


        FromFile search = new FromFile();
        search.setBIFFile(unique);

        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = new Instances(
                new BufferedReader(new StringReader(arff)));

        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).name().equals(target)) {
                data.setClassIndex(i);
                break;
            }
        }

        network.setEstimator(new SimpleEstimator());

        network.buildClassifier(data);


        ExpectationMaximizationWekaResponse response = new ExpectationMaximizationWekaResponse();

        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(network, data, FOLDS, new Random(1));
        response.setScvAuc(eval.weightedAreaUnderROC());

        response.setWeka(network);
        response.setNodes(fromWekaBif(network.graph()));
        File f = new File(unique);
        f.delete();
        return response;
    }

    private static void printBIF(String bif, String path) {
        File bifFile = new File(path);
        List<String> data = Arrays.stream(bif.split("\n")).collect(Collectors.toList());
        try (PrintWriter pw = new PrintWriter(bifFile)) {
            data.stream()
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
