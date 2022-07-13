package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.weka.performance.tests.util.Performance;
import org.junit.jupiter.api.Test;

public class test {
    @Test
    public void test() throws Exception {
        long start = System.currentTimeMillis();
        Performance p = DiabetesFewestBins.kFoldUnknown(0.3);
        System.out.println("text");
        p = DiabetesFewestBins.kFoldUnknown(0.05);

    }
}
