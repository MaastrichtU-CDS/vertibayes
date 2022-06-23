package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.Performance;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;

import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildAsiaNetwork;
import static com.florian.vertibayes.weka.performance.Util.readData;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Asia {

    public static final String TEST_FULL = "resources/Experiments/asia/Asia10KWEKA.arff";
    public static final String TEST_FULL_MISSING = "resources/Experiments/asia/asia10kMissing.arff";

    public static final String FIRSTHALF = "resources/Experiments/asia/Asia10k_firsthalf.csv";
    public static final String SECONDHALF = "resources/Experiments/asia/Asia10k_secondhalf.csv";

    public static final String FOLD_LEFTHALF = "resources/Experiments/asia/folds/asiaLeftSplit";
    public static final String FOLD_RIGHTHALF = "resources/Experiments/asia/folds/asiaRightSplit";

    public static final String FOLD_LEFTHALF_MISSING = "resources/Experiments/asia/folds/asiamissingLeftSplit";
    public static final String FOLD_RIGHTHALF_MISSING = "resources/Experiments/asia/folds" +
            "/asiamissingRightSplit";

    public static final String FIRSTHALF_MISSING = "resources/Experiments/asia/asia10kMissingLeft.csv";
    public static final String SECONDHALF_MISSING = "resources/Experiments/asia/asia10kMissingRight.csv";

    public static final String TEST_FOLD = "resources/Experiments/asia/folds/asia";

    public static final String ASIA_WEKA_BIF = "resources/Experiments/asia/asiabif.xml";

    private static final String LABEL = "lung";
    private static final List<WebNode> nodes = buildAsiaNetwork();


    public static Performance kFoldUnknown(double treshold) throws Exception {
        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING, TEST_FOLD,
                                                                         LABEL, nodes);
        Performance p = test.kFoldTest(treshold);
        double asiaUnknown = Asia.weka(treshold);
        p.setWekaAuc(asiaUnknown);

        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.78, 0.025);
            assertEquals(p.getSyntheticAuc(), 0.78, 0.025);
            assertEquals(p.getSyntheticFoldAuc(), 0.78, 0.025);
        } else if (treshold == 0.01) {
            assertEquals(p.getRealAuc(), 0.7, 0.025);
            assertEquals(p.getSyntheticAuc(), 0.7, 0.025);
            assertEquals(p.getSyntheticFoldAuc(), 0.7, 0.025);
        }
        assertEquals(asiaUnknown, p.getRealAuc(), 0.025);
        assertEquals(asiaUnknown, p.getSyntheticAuc(), 0.025);
        assertEquals(asiaUnknown, p.getSyntheticFoldAuc(), 0.025);


        return p;
    }

    public static Performance kFold() throws Exception {
        PerformanceTestBase test = new PerformanceTestBase(FOLD_LEFTHALF,
                                                           FOLD_RIGHTHALF, TEST_FOLD,
                                                           LABEL, nodes);
        Performance p = test.kFoldTest();

        double asiaWeka = Asia.weka();
        p.setWekaAuc(asiaWeka);

        assertEquals(p.getRealAuc(), 0.99, 0.025);
        assertEquals(p.getSyntheticAuc(), 0.99, 0.025);
        assertEquals(p.getSyntheticFoldAuc(), 0.99, 0.025);

        assertEquals(asiaWeka, p.getRealAuc(), 0.025);
        assertEquals(asiaWeka, p.getSyntheticAuc(), 0.025);
        assertEquals(asiaWeka, p.getSyntheticFoldAuc(), 0.025);

        return p;
    }

    public static double weka(double treshold) throws Exception {
        return wekaTest(LABEL,
                        ASIA_WEKA_BIF.replace("Missing",
                                              "Treshold" + String.valueOf(treshold)
                                                      .replace(".", "_")),
                        TEST_FULL_MISSING.replace("Missing",
                                                  "MissingTreshold" + String.valueOf(treshold)
                                                          .replace(".", "_")));
    }

    public static double weka() throws Exception {
        return wekaTest("lung", ASIA_WEKA_BIF, TEST_FULL);
    }

    public static void testVertiBayesFullDataSet() throws Exception {
        double auc = buildAndValidate(FIRSTHALF, SECONDHALF, readData(LABEL, TEST_FULL),
                                      LABEL, TEST_FULL.replace("WEKA.arff", ".csv"),
                                      nodes).getRealAuc();


        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.98, 0.025);
    }

    public static void testVertiBayesFullDataSetMissing(double treshold) throws Exception {
        String first = FIRSTHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        double auc = buildAndValidate(first, second,
                                      readData(LABEL, full), LABEL, full.replace(".arff",
                                                                                 ".csv"), nodes).getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.78, 0.025);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.70, 0.04);
        }
    }
}
