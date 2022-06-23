package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.Performance;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;

import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.*;
import static com.florian.vertibayes.weka.performance.Util.readData;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Iris {

    public static final String TEST_FULL = "resources/Experiments/iris/irisWeka.arff";
    public static final String TEST_FULL_MISSING = "resources/Experiments/iris/irisMissing.arff";

    public static final String FIRSTHALF = "resources/Experiments/iris/iris_firsthalf.csv";
    public static final String SECONDHALF = "resources/Experiments/iris/iris_secondhalf.csv";

    public static final String FOLD_LEFTHALF = "resources/Experiments/iris/folds/irisLeftSplit";
    public static final String FOLD_RIGHTHALF = "resources/Experiments/iris/folds/irisRightSplit";

    public static final String FOLD_LEFTHALF_MISSING = "resources/Experiments/iris/folds/irismissingLeftSplit";
    public static final String FOLD_RIGHTHALF_MISSING = "resources/Experiments/iris/folds/irismissingRightSplit";

    public static final String FIRSTHALF_MISSING = "resources/Experiments/iris/irisMissingLeft.csv";
    public static final String SECONDHALF_MISSING = "resources/Experiments/iris/irisMissingRight.csv";

    public static final String TEST_FOLD = "resources/Experiments/iris/folds/iris";

    public static final String IRIS_WEKA_BIF = "resources/Experiments/iris/irisWekaBif.xml";

    private static final String LABEL = "label";

    public static Performance kFoldUnknown(boolean automaticBinning, double treshold) throws Exception {
        List<WebNode> nodes;
        if (automaticBinning) {
            nodes = buildIrisNetworkNoBins();
        } else {
            nodes = buildIrisNetworkBinnedMissing();
        }

        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING, TEST_FOLD,
                                                                         LABEL, nodes);
        Performance p = test.kFoldTest(treshold);
        double irisUnknown = Iris.weka(treshold);
        p.setWekaAuc(irisUnknown);

        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.96, 0.04);
            assertEquals(p.getSyntheticAuc(), 0.96, 0.04);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
        } else if (treshold == 0.1) {
            assertEquals(p.getRealAuc(), 0.96, 0.04);
            assertEquals(p.getSyntheticAuc(), 0.96, 0.04);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
        }

        assertEquals(irisUnknown, p.getRealAuc(), 0.025);
        assertEquals(irisUnknown, p.getSyntheticAuc(), 0.025);
        // Synthetic fold AUC for iris is all over the place due to the small folds
        // So ignore it

        return p;
    }

    public static Performance kFold(boolean automaticBinning) throws Exception {
        List<WebNode> nodes;
        if (automaticBinning) {
            nodes = buildIrisNetworkNoBins();
        } else {
            nodes = buildIrisNetworkBinned();
        }

        PerformanceTestBase test = new PerformanceTestBase(FOLD_LEFTHALF,
                                                           FOLD_RIGHTHALF, TEST_FOLD,
                                                           LABEL, nodes);
        Performance p = test.kFoldTest();
        double iris = Iris.weka();
        p.setWekaAuc(iris);
        if (automaticBinning) {
            assertEquals(p.getRealAuc(), 0.96, 0.05);
            assertEquals(p.getSyntheticAuc(), 0.96, 0.05);
        } else {
            assertEquals(p.getRealAuc(), 0.90, 0.1);
            assertEquals(p.getSyntheticAuc(), 0.90, 0.1);
        }

        assertEquals(iris, p.getRealAuc(), 0.025);
        assertEquals(iris, p.getSyntheticAuc(), 0.025);
        // Synthetic fold AUC for iris is all over the place due to the small folds
        // So ignore it
        return p;
    }

    public static double weka(double treshold) throws Exception {
        return wekaTest("label",
                        IRIS_WEKA_BIF.replace("Missing",
                                              "MissingTreshold" + String.valueOf(
                                                              treshold)
                                                      .replace(".", "_"))
                , TEST_FULL_MISSING.replace("Missing",
                                            "MissingTreshold" + String.valueOf(treshold)
                                                    .replace(".", "_")));
    }

    public static double weka() throws Exception {
        return wekaTest(LABEL, IRIS_WEKA_BIF, TEST_FULL);
    }

    public static void testVertiBayesFullDataSet() throws Exception {
        double aucManual = buildAndValidate(FIRSTHALF, SECONDHALF,
                                            readData(LABEL, TEST_FULL), LABEL,
                                            TEST_FULL.replace("Weka.arff",
                                                              ".csv"), buildIrisNetworkBinned()).getRealAuc();

        double aucAutomatic = buildAndValidate(FIRSTHALF, SECONDHALF,
                                               readData(LABEL, TEST_FULL), LABEL,
                                               TEST_FULL.replace("Weka.arff",
                                                                 ".csv"), buildIrisNetworkNoBins()).getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(aucAutomatic, 0.98, 0.025);
        assertEquals(aucManual, 0.98, 0.025);
    }

    public static void testVertiBayesFullDataSetMissing(double treshold) throws Exception {
        String first = FIRSTHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));

        double auc = buildAndValidate(first, second,
                                      readData(LABEL, full), LABEL,
                                      full.replace(".arff",
                                                   ".csv"), buildIrisNetworkBinnedMissing()).getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        // check AUC depending on the degree of missing data
        if (treshold == 0.05) {
            assertEquals(auc, 0.98, 0.025);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.98, 0.025);
        }
    }
}
