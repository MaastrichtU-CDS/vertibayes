package com.florian.vertibayes.weka.performance.tests.util;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;
import static com.florian.vertibayes.weka.performance.tests.util.Util.recordErrors;

public class WekaPerformance {

    private static int FOLDS = 10;
    private static List<Integer> folds;

    private static void initFolds() {
        folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
    }

    public static double wekaTest(String target, String biff, String arff) throws Exception {
        initFolds();
        FromFile search = new FromFile();
        search.setBIFFile(biff);

        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = readData(target, arff);

        Evaluation eval = new Evaluation(data);
        network.buildClassifier(data);
        eval.crossValidateModel(network, data, 10, new Random(1));

        return eval.weightedAreaUnderROC();

    }

    public static Performance wekaGenerateErrors(String target, String biff, String arff)
            throws Exception {
        initFolds();
        Performance res = new Performance();
        for (int i = 0; i < folds.size(); i++) {
            res.getWekaErrors().putAll(generateErrors(target, biff, arff, folds.get(i)));
        }
        return res;
    }

    private static Map<String, List<Integer>> generateErrors(String target, String biff, String arff, int fold)
            throws Exception {
        FromFile search = new FromFile();
        search.setBIFFile(biff);

        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);

        String test = arff.replace(".arff", fold + "WEKA.arff");

        Instances testData = readData(target, test);
        List<Integer> included = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
        Instances trainData = createTrainData(target, included, arff);

        Evaluation eval = new Evaluation(trainData);
        network.buildClassifier(trainData);
        Map<String, List<Integer>> errors = new HashMap<>();
        errors.put(test, recordErrors(network, testData));
        return errors;
    }

    private static String insertFold(int index, String arff) {
        String path = "";
        for (int i = 0; i < index; i++) {
            path += arff.charAt(i);
        }
        path += "/fold";
        for (int i = index; i < arff.length(); i++) {
            path += arff.charAt(i);
        }
        return path;
    }

    private static Instances createTrainData(String target, List<Integer> included, String arff) throws IOException {
        Instances trainData = null;
        for (int i = 0; i < included.size(); i++) {
            Instances additional = readData(target, arff.replace(".arff", included.get(i) + "WEKA.arff"));
            if (trainData == null) {
                trainData = additional;
            } else {
                for (int j = 0; j < additional.size(); j++) {
                    trainData.add(additional.instance(j));
                }
            }
        }
        return trainData;
    }

}
