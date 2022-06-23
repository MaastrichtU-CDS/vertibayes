package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.Performance;

import java.util.ArrayList;
import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildAsiaNetwork;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;

public class AsiaTest {

    public static final String TEST_ASIA_FULL = "resources/Experiments/asia/Asia10KWEKA.arff";

    public static final String FOLD_LEFTHALF_ASIA = "resources/Experiments/asia/folds/asiaLeftSplit";
    public static final String TEST_FOLD_ASIA = "resources/Experiments/asia/folds/asia";
    public static final String FOLD_RIGHTHALF_ASIA = "resources/Experiments/asia/folds/asiaRightSplit";

    public static final String FOLD_LEFTHALF_ASIA_MISSING = "resources/Experiments/asia/folds/asiamissingLeftSplit";
    public static final String FOLD_RIGHTHALF_ASIA_MISSING = "resources/Experiments/asia/folds" +
            "/asiamissingRightSplit";

    public static final String ASIA_WEKA_BIF = "resources/Experiments/asia/asiabif.xml";

    public static final String TEST_ASIA_FULL_MISSING = "resources/Experiments/asia/asia10kMissing.arff";

    private static final String LABEL = "lung";
    private int FOLDS = 10;
    private List<Integer> folds;


    public AsiaTest() {
        folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
    }

    public Performance kFoldUnknown(double treshold) throws Exception {
        List<WebNode> nodes = buildAsiaNetwork();

        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_ASIA_MISSING,
                                                                         FOLD_RIGHTHALF_ASIA_MISSING, TEST_FOLD_ASIA,
                                                                         LABEL, nodes);
        return test.kFoldTest(folds, treshold);
    }

    public Performance kFold() throws Exception {
        List<WebNode> nodes = buildAsiaNetwork();

        PerformanceTestBase test = new PerformanceTestBase(FOLD_LEFTHALF_ASIA,
                                                           FOLD_RIGHTHALF_ASIA, TEST_FOLD_ASIA,
                                                           LABEL, nodes);
        return test.kFoldTest(folds);
    }

    public double weka(double treshold) throws Exception {
        return wekaTest(LABEL,
                        ASIA_WEKA_BIF.replace("Missing",
                                              "Treshold" + String.valueOf(treshold)
                                                      .replace(".", "_")),
                        TEST_ASIA_FULL_MISSING.replace("Missing",
                                                       "MissingTreshold" + String.valueOf(treshold)
                                                               .replace(".", "_")));
    }

    public double weka() throws Exception {
        return wekaTest("lung", ASIA_WEKA_BIF, TEST_ASIA_FULL);
    }
}
