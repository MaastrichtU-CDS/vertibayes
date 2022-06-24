package com.florian.vertibayes.weka.performance.tests.base;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.tests.util.Performance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildAsiaNetwork;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;

public class PerformanceTestBase {
    private final String FOLD_LEFTHALF;
    private final String FOLD_RIGHTHALF;
    private final String TEST_FOLD;
    private final String LABEL;
    private final List<WebNode> NODES;

    private int FOLDS = 10;
    private static List<Integer> folds;

    public PerformanceTestBase(String left, String right, String test, String label, List<WebNode> nodes) {

        this.FOLD_LEFTHALF = left;
        this.FOLD_RIGHTHALF = right;
        this.TEST_FOLD = test;
        this.LABEL = label;
        this.NODES = nodes;

        folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
    }

    public List<Performance> kFoldTest()
            throws Exception {
        List<Performance> performances = new ArrayList<>();
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF + ids + ".csv";
            String right = FOLD_RIGHTHALF + ids + ".csv";

            String testFoldarrf = TEST_FOLD + fold + "WEKA.arff";
            String testFoldcsv = TEST_FOLD + fold + ".csv";

            Performance res = buildAndValidate(left, right,
                                               readData("lung", testFoldarrf),
                                               "lung", testFoldcsv, buildAsiaNetwork());
            performances.add(res);

        }
        return performances;

    }


}
