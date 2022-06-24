package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;
import com.florian.vertibayes.weka.performance.tests.util.Performance;
import com.florian.vertibayes.weka.performance.tests.util.Variance;

import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildIrisNetworkBinned;
import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildIrisNetworkBinnedMissing;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.averagePerformance;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.checkVariance;
import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IrisManual {

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
    private static final String NAME = "IrisManual";

    private static final List<WebNode> NODES = buildIrisNetworkBinned();

    private static final double AVERAGERROR = 0.025;
    private static final Variance FOLDVARIANCE;
    private static final Variance FOLDVARIANCEMISSING;

    private static final double MINPERCENTAGE = 0.1;

    static {
        FOLDVARIANCE = new Variance();
        FOLDVARIANCE.setRealAucVariance(0.05);
        FOLDVARIANCE.setSyntheticAucVariance(0.05);
        FOLDVARIANCE.setSyntheticFoldAucVariance(0.05);

        FOLDVARIANCEMISSING = new Variance();
        FOLDVARIANCEMISSING.setRealAucVariance(0.06);
        FOLDVARIANCEMISSING.setSyntheticAucVariance(0.06);
        FOLDVARIANCEMISSING.setSyntheticFoldAucVariance(0.06);
    }

    public static Performance kFoldUnknown(double treshold) throws Exception {
        List<WebNode> nodes;
        nodes = buildIrisNetworkBinnedMissing();

        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING, TEST_FOLD,
                                                                         LABEL, nodes, MINPERCENTAGE);
        List<Performance> performances = test.kFoldTest(treshold);
        Performance p = averagePerformance(performances);
        checkVariance(performances, p, FOLDVARIANCEMISSING);
        p.setName(NAME);
        double irisUnknown = IrisManual.weka(treshold);
        p.setWekaAuc(irisUnknown);

        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.96, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.96, AVERAGERROR);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
        } else if (treshold == 0.1) {
            assertEquals(p.getRealAuc(), 0.96, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.96, AVERAGERROR);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
        } else if (treshold == 0.3) {
            assertEquals(p.getRealAuc(), 0.96, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.96, AVERAGERROR);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
        }

        assertEquals(irisUnknown, p.getRealAuc(), AVERAGERROR);
        assertEquals(irisUnknown, p.getSyntheticAuc(), AVERAGERROR);
        // Synthetic fold AUC for iris is all over the place due to the small folds
        // So ignore it

        return p;
    }

    public static Performance kFold() throws Exception {
        PerformanceTestBase test = new PerformanceTestBase(FOLD_LEFTHALF,
                                                           FOLD_RIGHTHALF, TEST_FOLD,
                                                           LABEL, NODES, MINPERCENTAGE);
        List<Performance> performances = test.kFoldTest();
        Performance p = averagePerformance(performances);
        checkVariance(performances, p, FOLDVARIANCE);
        p.setName(NAME);
        double iris = IrisManual.weka();
        p.setWekaAuc(iris);
        assertEquals(p.getRealAuc(), 0.90, AVERAGERROR);
        assertEquals(p.getSyntheticAuc(), 0.90, AVERAGERROR);


        assertEquals(iris, p.getRealAuc(), AVERAGERROR);
        assertEquals(iris, p.getSyntheticAuc(), AVERAGERROR);
        // Synthetic fold AUC for iris is all over the place due to the small folds
        // So ignore it
        return p;
    }

    private static double weka(double treshold) throws Exception {
        return wekaTest("label",
                        IRIS_WEKA_BIF.replace("Missing",
                                              "MissingTreshold" + String.valueOf(
                                                              treshold)
                                                      .replace(".", "_"))
                , TEST_FULL_MISSING.replace("Missing",
                                            "MissingTreshold" + String.valueOf(treshold)
                                                    .replace(".", "_")));
    }

    private static double weka() throws Exception {
        return wekaTest(LABEL, IRIS_WEKA_BIF, TEST_FULL);
    }

    public static void testVertiBayesFullDataSet() throws Exception {
        double auc = buildAndValidate(FIRSTHALF, SECONDHALF,
                                      readData(LABEL, TEST_FULL), LABEL,
                                      TEST_FULL.replace("Weka.arff",
                                                        ".csv"), NODES, MINPERCENTAGE).getRealAuc();
        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.98, AVERAGERROR);
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
                                                   ".csv"), NODES, MINPERCENTAGE).getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        // check AUC depending on the degree of missing data
        if (treshold == 0.05) {
            assertEquals(auc, 0.98, AVERAGERROR);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.96, AVERAGERROR);
        } else if (treshold == 0.3) {
            assertEquals(auc, 0.96, AVERAGERROR);
        }
    }
}
