package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.Performance;

import java.util.ArrayList;
import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildIrisNetworkBinnedMissing;
import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildIrisNetworkNoBins;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;

public class IrisTest {

    public static final String FOLD_LEFTHALF_IRIS_MISSING = "resources/Experiments/iris/folds/irismissingLeftSplit";
    public static final String FOLD_RIGHTHALF_IRIS_MISSING = "resources/Experiments/iris/folds/irismissingRightSplit";
    public static final String TEST_FOLD_IRIS = "resources/Experiments/iris/folds/iris";


    public static final String TEST_IRIS_FULL_MISSING = "resources/Experiments/iris/irisMissing.arff";

    public static final String IRIS_WEKA_BIF = "resources/Experiments/iris/irisWekaBif.xml";

    private static final String LABEL = "label";
    private int FOLDS = 10;
    private List<Integer> folds;

    public IrisTest() {
        folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
    }

    public Performance kFoldUnknown(boolean automaticBinning, double treshold) throws Exception {
        List<WebNode> nodes;
        if (automaticBinning) {
            nodes = buildIrisNetworkNoBins();
        } else {
            nodes = buildIrisNetworkBinnedMissing();
        }

        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_IRIS_MISSING,
                                                                         FOLD_RIGHTHALF_IRIS_MISSING, TEST_FOLD_IRIS,
                                                                         LABEL, nodes);
        return test.kFoldTest(folds, treshold);
    }

    public double weka(double treshold) throws Exception {
        return wekaTest("label",
                        IRIS_WEKA_BIF.replace("Missing",
                                              "MissingTreshold" + String.valueOf(
                                                              treshold)
                                                      .replace(".", "_"))
                , TEST_IRIS_FULL_MISSING.replace("Missing",
                                                 "MissingTreshold" + String.valueOf(treshold)
                                                         .replace(".", "_")));
    }
}
