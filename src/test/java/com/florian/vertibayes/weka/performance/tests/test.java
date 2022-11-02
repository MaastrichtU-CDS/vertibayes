package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.weka.performance.tests.util.Performance;
import org.junit.jupiter.api.Test;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.estimate.SimpleEstimator;
import weka.classifiers.bayes.net.search.local.K2;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;

import static com.florian.vertibayes.weka.performance.TestPerformance.printResults;

public class test {
    @Test
    public void test() throws Exception {
        long start = System.currentTimeMillis();
        Performance p = Asia.testVertiBayesFullDataSet();
        p.setName("test");
        printResults(start, p, 0.05, true);

        //79124

    }

    @Test
    public void test2() throws Exception {
        BayesNet network = new BayesNet();
        Instances data = new Instances(
                new BufferedReader(new FileReader("resources/Experiments/k2/missingString_Complete.arff")));
        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).name().equals("x3")) {
                data.setClassIndex(i);
                break;
            }
        }

        network.setEstimator(new SimpleEstimator());
        K2 k2 = new K2();
        k2.setInitAsNaiveBayes(false);
        network.setSearchAlgorithm(k2);

        network.buildClassifier(data);
        System.out.println(network.graph());
    }
}
