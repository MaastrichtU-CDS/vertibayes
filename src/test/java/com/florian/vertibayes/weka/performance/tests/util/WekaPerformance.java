package com.florian.vertibayes.weka.performance.tests.util;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;

import java.util.Random;

import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;

public class WekaPerformance {

    public static double wekaTest(String target, String biff, String arff) throws Exception {
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
}
