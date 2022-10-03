package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.weka.performance.tests.util.Performance;
import org.junit.jupiter.api.Test;

import static com.florian.vertibayes.weka.performance.TestPerformance.printResults;

public class test {
    @Test
    public void test() throws Exception {
        long start = System.currentTimeMillis();
//        Performance p = DiabetesDiscrete.testVertiBayesFullDataSet();
//        DiabetesDiscrete.testVertiBayesFullDataSetMissing(0.05);
//        DiabetesDiscrete.testVertiBayesFullDataSetMissing(0.10);
//        DiabetesDiscrete.testVertiBayesFullDataSetMissing(0.30);
//        Performance p = DiabetesDiscrete.kFoldUnknown(0.30);
//        p.setName("test");
//        printResults(start, p, 0.05, true);
//        p = DiabetesDiscrete.kFoldUnknown(0.05);
//        p.setName("test");
//        printResults(start, p, 0.05, true);
//        Performance p = DiabetesDiscrete.kFoldUnknown(0.1);
//        p.setName("test");
//        printResults(start, p, 0.05, true);
        Performance p = DiabetesDiscrete.kFold();
        p.setName("test");
        printResults(start, p, 0.05, true);

    }
}
