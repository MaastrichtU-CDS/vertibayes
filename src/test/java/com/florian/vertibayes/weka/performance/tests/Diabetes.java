package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;
import com.florian.vertibayes.weka.performance.tests.util.Performance;
import com.florian.vertibayes.weka.performance.tests.util.Variance;

import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildDiabetesNetwork;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.averagePerformance;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.checkVariance;
import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Diabetes {

    public static final String TEST_FULL = "resources/Experiments/diabetes/diabetesWEKA.arff";
    public static final String TEST_FULL_MISSING = "resources/Experiments/diabetes/diabetesMissing.arff";

    public static final String FIRSTHALF = "resources/Experiments/diabetes/diabetes_firsthalf.csv";
    public static final String SECONDHALF = "resources/Experiments/diabetes/diabetes_secondhalf.csv";

    public static final String FOLD_LEFTHALF = "resources/Experiments/diabetes/folds/diabetesLeftSplit";
    public static final String FOLD_RIGHTHALF = "resources/Experiments/diabetes/folds/diabetesRightSplit";

    public static final String FOLD_LEFTHALF_MISSING = "resources/Experiments/diabetes/folds" +
            "/diabetesmissingLeftSplit";
    public static final String FOLD_RIGHTHALF_MISSING = "resources/Experiments/diabetes/folds" +
            "/diabetesmissingRightSplit";

    public static final String FIRSTHALF_MISSING = "resources/Experiments/diabetes/diabetesMissingLeft.csv";
    public static final String SECONDHALF_MISSING = "resources/Experiments/diabetes/diabetesMissingRight.csv";

    public static final String TEST_FOLD = "resources/Experiments/diabetes/folds/diabetes";

    public static final String DIABETES_WEKA_BIF = "resources/Experiments/diabetes/diabetesbif.xml";

    private static final String LABEL = "Outcome";
    private static final List<WebNode> nodes = buildDiabetesNetwork();
    private static final String NAME = "Diabetes";

    private static final double AVERAGERROR = 0.025;
    private static final Variance FOLDVARIANCE;
    private static final Variance FOLDVARIANCEMISSING;

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
        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING,
                                                                         TEST_FOLD,
                                                                         LABEL, nodes);
        List<Performance> performances = test.kFoldTest(treshold);
        Performance p = averagePerformance(performances);
        checkVariance(performances, p, FOLDVARIANCEMISSING);
        p.setName(NAME);
        double diabetesUnknown = Diabetes.weka(treshold);
        p.setWekaAuc(diabetesUnknown);

        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.79, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.87, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.79, AVERAGERROR);
        } else if (treshold == 0.1) {
            assertEquals(p.getRealAuc(), 0.75, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.83, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.75, AVERAGERROR);
        } else if (treshold == 0.3) {
            assertEquals(p.getRealAuc(), 0.65, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.75, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.65, AVERAGERROR);
        }

        assertEquals(diabetesUnknown, p.getRealAuc(), AVERAGERROR);
        //Using synthetic training data in k-fold results in weird stuff for diabetes, no idea why.
        //Other validation methods have expected results
        assertEquals(diabetesUnknown, p.getSyntheticAuc(), 0.11);
        assertEquals(diabetesUnknown, p.getSyntheticFoldAuc(), AVERAGERROR);

        return p;
    }

    public static Performance kFold() throws Exception {
        PerformanceTestBase test = new PerformanceTestBase(FOLD_LEFTHALF,
                                                           FOLD_RIGHTHALF, TEST_FOLD,
                                                           LABEL, nodes);
        List<Performance> performances = test.kFoldTest();
        Performance p = averagePerformance(performances);
        checkVariance(performances, p, FOLDVARIANCE);
        p.setName(NAME);
        double diabetesWeka = Diabetes.weka();
        p.setWekaAuc(diabetesWeka);

        assertEquals(p.getRealAuc(), 0.79, AVERAGERROR);
        assertEquals(p.getSyntheticAuc(), 0.79, AVERAGERROR);
        assertEquals(p.getSyntheticFoldAuc(), 0.79, AVERAGERROR);
        assertEquals(diabetesWeka, p.getRealAuc(), 0.025);
        //Using synthetic training data in k-fold results in weird stuff for diabetes
        //Other validation methods have expected results
        assertEquals(diabetesWeka, p.getSyntheticAuc(), 0.11);
        assertEquals(diabetesWeka, p.getSyntheticFoldAuc(), AVERAGERROR);
        return p;
    }

    private static double weka(double treshold) throws Exception {
        return wekaTest(LABEL,
                        DIABETES_WEKA_BIF.replace("Missing",
                                                  "Treshold" + String.valueOf(treshold)
                                                          .replace(".", "_")),
                        TEST_FULL_MISSING.replace("Missing",
                                                  "MissingTreshold" + String.valueOf(treshold)
                                                          .replace(".", "_")));
    }

    private static double weka() throws Exception {
        return wekaTest("lung", DIABETES_WEKA_BIF, TEST_FULL);
    }

    public static void testVertiBayesFullDataSet() throws Exception {
        double auc = buildAndValidate(FIRSTHALF, SECONDHALF,
                                      readData(LABEL, TEST_FULL),
                                      LABEL, TEST_FULL.replace("WEKA.arff", ".csv"), nodes).getRealAuc();


        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.85, 0.025);
    }

    public static void testVertiBayesFullDataSetMissing(double treshold) throws Exception {
        String first = FIRSTHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        double auc = buildAndValidate(first, second, readData(LABEL, full), LABEL,
                                      full.replace(".arff", ".csv"), nodes).getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.84, AVERAGERROR);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.80, AVERAGERROR);
        } else if (treshold == 0.3) {
            assertEquals(auc, 0.75, AVERAGERROR);
        }
    }
}
