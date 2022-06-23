package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.Performance;

import java.util.List;
import java.util.stream.Collectors;

import static com.florian.vertibayes.weka.performance.Util.readData;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;

public class PerformanceMissingTestBase {
    private final String FOLD_LEFTHALF;
    private final String FOLD_RIGHTHALF;
    private final String TEST_FOLD;
    private final String LABEL;
    private final List<WebNode> NODES;

    public PerformanceMissingTestBase(String left, String right, String test, String label, List<WebNode> nodes) {

        this.FOLD_LEFTHALF = left;
        this.FOLD_RIGHTHALF = right;
        this.TEST_FOLD = test;
        this.LABEL = label;
        this.NODES = nodes;
    }

    public Performance kFoldTest(List<Integer> folds, double treshold)
            throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
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
                                   LABEL, testFoldCsv, NODES);


            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();

        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }


}
