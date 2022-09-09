package com.florian.vertibayes.weka.performance.tests.base;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.tests.util.Performance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;
import static com.florian.vertibayes.weka.performance.tests.util.VertiBayesPerformance.buildAndValidate;

public class PerformanceMissingTestBase {
    private final String FOLD_LEFTHALF;
    private final String FOLD_RIGHTHALF;
    private final String TEST_FOLD;
    private final String LABEL;
    private final List<WebNode> NODES;
    private final double MINPERCENTAGE;
    private final Instances FULL_DATA_SET;

    private int FOLDS = 10;
    private static List<Integer> folds;

    public PerformanceMissingTestBase(String left, String right, String test, String label, List<WebNode> nodes,
                                      double minPercentage, Instances fullDataSet) {

        this.FOLD_LEFTHALF = left;
        this.FOLD_RIGHTHALF = right;
        this.TEST_FOLD = test;
        this.LABEL = label;
        this.NODES = nodes;
        this.MINPERCENTAGE = minPercentage;
        this.FULL_DATA_SET = fullDataSet;

        folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
    }

    public List<Performance> kFoldTest(double treshold)
            throws Exception {
        List<Performance> performances = new ArrayList<>();
        //unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF.replace("missing",
                                                "Treshold" + String.valueOf(treshold)
                                                        .replace(".", "_") + "missing") + ids + ".csv";
            String right = FOLD_RIGHTHALF.replace("missing",
                                                  "Treshold" + String.valueOf(treshold)
                                                          .replace(".", "_") + "missing") + ids + ".csv";
            Performance res = null;

            String testFoldarff = TEST_FOLD + "Treshold" + String.valueOf(
                    treshold).replace(".", "_") +
                    "missing" + fold + "WEKA.arff";
            String testFoldCsv = testFoldarff.replace("WEKA.arff", ".csv");
            res = buildAndValidate(left, right,
                                   readData(LABEL, testFoldarff),
                                   LABEL, testFoldCsv, NODES, MINPERCENTAGE, FULL_DATA_SET);


            performances.add(res);
        }
        return performances;
    }
}
