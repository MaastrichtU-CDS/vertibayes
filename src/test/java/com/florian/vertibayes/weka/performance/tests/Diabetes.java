package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.Performance;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;

import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildDiabetesNetwork;
import static com.florian.vertibayes.weka.performance.Util.readData;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;
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

    public static Performance kFoldUnknown(double treshold) throws Exception {
        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING,
                                                                         TEST_FOLD,
                                                                         LABEL, nodes);
        Performance p = test.kFoldTest(treshold);
        double diabetesUnknown = Diabetes.weka(treshold);
        p.setWekaAuc(diabetesUnknown);

        //the difference between a good and a bad fold can be quite big here dependin on RNG.
        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.76, 0.1);
            assertEquals(p.getSyntheticAuc(), 0.76, 0.15);
            assertEquals(p.getSyntheticFoldAuc(), 0.76, 0.11);
        } else if (treshold == 0.1) {
            assertEquals(p.getRealAuc(), 0.74, 0.1);
            assertEquals(p.getSyntheticAuc(), 0.74, 0.15);
            assertEquals(p.getSyntheticFoldAuc(), 0.74, 0.11);
        } else if (treshold == 0.3) {
            assertEquals(p.getRealAuc(), 0.73, 0.1);
            assertEquals(p.getSyntheticAuc(), 0.73, 0.15);
            assertEquals(p.getSyntheticFoldAuc(), 0.73, 0.11);
        }

        assertEquals(diabetesUnknown, p.getRealAuc(), 0.025);
        //Using synthetic training data in k-fold results in weird stuff for diabetes, no idea why.
        //Other validation methods have expected results
        assertEquals(diabetesUnknown, p.getSyntheticAuc(), 0.11);
        assertEquals(diabetesUnknown, p.getSyntheticFoldAuc(), 0.025);

        return p;
    }

    public static Performance kFold() throws Exception {
        PerformanceTestBase test = new PerformanceTestBase(FOLD_LEFTHALF,
                                                           FOLD_RIGHTHALF, TEST_FOLD,
                                                           LABEL, nodes);
        Performance p = test.kFoldTest();
        double diabetesWeka = Diabetes.weka();
        p.setWekaAuc(diabetesWeka);

        assertEquals(p.getRealAuc(), 0.79, 0.2);
        assertEquals(p.getSyntheticAuc(), 0.79, 0.2);
        assertEquals(p.getSyntheticFoldAuc(), 0.79, 0.2);
        assertEquals(diabetesWeka, p.getRealAuc(), 0.025);
        //Using synthetic training data in k-fold results in weird stuff for diabetes
        //Other validation methods have expected results
        assertEquals(diabetesWeka, p.getSyntheticAuc(), 0.11);
        assertEquals(diabetesWeka, p.getSyntheticFoldAuc(), 0.025);
        return p;
    }

    public static double weka(double treshold) throws Exception {
        return wekaTest(LABEL,
                        DIABETES_WEKA_BIF.replace("Missing",
                                                  "Treshold" + String.valueOf(treshold)
                                                          .replace(".", "_")),
                        TEST_FULL_MISSING.replace("Missing",
                                                  "MissingTreshold" + String.valueOf(treshold)
                                                          .replace(".", "_")));
    }

    public static double weka() throws Exception {
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
            assertEquals(auc, 0.84, 0.025);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.80, 0.025);
        } else if (treshold == 0.3) {
            assertEquals(auc, 0.75, 0.025);
        }
    }
}
