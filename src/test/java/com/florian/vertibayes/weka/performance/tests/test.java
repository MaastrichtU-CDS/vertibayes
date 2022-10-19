package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.weka.performance.tests.util.Performance;
import org.junit.jupiter.api.Test;

import static com.florian.vertibayes.weka.performance.TestPerformance.printResults;

public class test {
    @Test
    public void test() throws Exception {
        long start = System.currentTimeMillis();
        Performance p = Asia.testVertiBayesFullDataSet();
        p = Asia.kFold();
        p = Asia.kFoldUnknown(0.05);
        p.setName("test");
        printResults(start, p, 0.05, true);

        //79124

    }
}
