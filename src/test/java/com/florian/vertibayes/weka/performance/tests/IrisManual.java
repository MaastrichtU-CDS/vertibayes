package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;
import com.florian.vertibayes.weka.performance.tests.util.Performance;
import com.florian.vertibayes.weka.performance.tests.util.Variance;
import weka.core.Instances;

import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildIrisNetworkBinned;
import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildIrisNetworkBinnedMissing;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.averagePerformance;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.checkVariance;
import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;
import static com.florian.vertibayes.weka.performance.tests.util.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.tests.util.WekaPerformance.wekaGenerateErrors;
import static com.florian.vertibayes.weka.performance.tests.util.WekaPerformance.wekaTest;
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

    private static List<WebNode> NODES = buildIrisNetworkBinned();

    private static final double AVERAGERROR = 0.05;
    private static final Variance FOLDVARIANCE;
    private static final Variance FOLDVARIANCEMISSING;

    private static final double MINPERCENTAGE = 0.1;

    static {
        FOLDVARIANCE = new Variance();
        FOLDVARIANCE.setRealAucVariance(0.20);
        FOLDVARIANCE.setSyntheticAucVariance(0.20);
        //Iris synthetic fold is weird
        FOLDVARIANCE.setSyntheticFoldAucVariance(1.0);

        FOLDVARIANCEMISSING = new Variance();
        FOLDVARIANCEMISSING.setRealAucVariance(0.20);
        FOLDVARIANCEMISSING.setSyntheticAucVariance(0.20);
        //Iris synthetic fold is weird
        FOLDVARIANCEMISSING.setSyntheticFoldAucVariance(1.0);
    }

    private static void initNodes() {
        NODES = buildIrisNetworkBinned();
    }

    public static Performance kFoldUnknown(double treshold) throws Exception {
        initNodes();
        List<WebNode> nodes = buildIrisNetworkBinnedMissing();
        String full = generateMissingFullPath(treshold);
        Instances fulldata = readData(LABEL, full);

        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING, TEST_FOLD,
                                                                         LABEL, nodes, MINPERCENTAGE, fulldata);
        List<Performance> performances = test.kFoldTest(treshold);
        Performance p = averagePerformance(performances);
        checkVariance(performances, p, FOLDVARIANCEMISSING);
        p.setName(NAME);
        Performance irisUnknown = IrisManual.weka(treshold);
        p.setWekaAuc(irisUnknown.getWekaAuc());
        p.setWekaErrors(irisUnknown.getWekaErrors());
        p.setWekaAIC(irisUnknown.getWekaAIC());
        p.matchErrors();

        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.98, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.98, AVERAGERROR);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
        } else if (treshold == 0.1) {
            assertEquals(p.getRealAuc(), 0.97, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.97, AVERAGERROR);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
        } else if (treshold == 0.3) {
            assertEquals(p.getRealAuc(), 0.92, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.92, AVERAGERROR);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
        }

        assertEquals(p.getWekaAuc(), p.getRealAuc(), AVERAGERROR);
        assertEquals(p.getWekaAuc(), p.getSyntheticAuc(), AVERAGERROR);
        // Synthetic fold AUC for iris is all over the place due to the small folds
        // So ignore it

        return p;
    }

    public static Performance kFold() throws Exception {
        initNodes();
        Instances fullData = readData(LABEL, TEST_FULL);
        PerformanceTestBase test = new PerformanceTestBase(FOLD_LEFTHALF,
                                                           FOLD_RIGHTHALF, TEST_FOLD,
                                                           LABEL, NODES, MINPERCENTAGE, fullData);
        List<Performance> performances = test.kFoldTest();
        Performance p = averagePerformance(performances);
        checkVariance(performances, p, FOLDVARIANCE);
        p.setName(NAME);
        Performance iris = IrisManual.weka();
        p.setWekaAuc(iris.getWekaAuc());
        p.setWekaErrors(iris.getWekaErrors());
        p.setWekaAIC(iris.getWekaAIC());
        p.matchErrors();

        assertEquals(p.getRealAuc(), 0.99, AVERAGERROR);
        assertEquals(p.getSyntheticAuc(), 0.99, AVERAGERROR);


        assertEquals(p.getWekaAuc(), p.getRealAuc(), AVERAGERROR);
        assertEquals(p.getWekaAuc(), p.getSyntheticAuc(), AVERAGERROR);
        // Synthetic fold AUC for p.getWekaAUC() is all over the place due to the small folds
        // So ignore it
        return p;
    }

    private static Performance weka(double treshold) throws Exception {
        String testFold = TEST_FOLD + "Treshold" + String.valueOf(treshold).replace(".", "_") + "missing.arff";

        Performance res = new Performance();
        Performance errors = wekaGenerateErrors(LABEL,
                                                IRIS_WEKA_BIF.replace("Missing",
                                                                      "Treshold" + String.valueOf(treshold)
                                                                              .replace(".", "_")),
                                                testFold);
        res.getWekaErrors().putAll(errors.getWekaErrors());

        res.setWekaAuc(wekaTest(LABEL,
                                IRIS_WEKA_BIF.replace("Missing",
                                                      "Treshold" + String.valueOf(treshold)
                                                              .replace(".", "_")),
                                TEST_FULL_MISSING.replace("Missing",
                                                          "MissingTreshold" + String.valueOf(treshold)
                                                                  .replace(".", "_")), res));
        return res;
    }

    private static Performance weka() throws Exception {
        String testFold = TEST_FOLD + ".arff";

        Performance res = new Performance();
        Performance errors = wekaGenerateErrors(LABEL, IRIS_WEKA_BIF, testFold);
        res.getWekaErrors().putAll(errors.getWekaErrors());
        res.setWekaAuc(wekaTest(LABEL, IRIS_WEKA_BIF, TEST_FULL, res));
        return res;
    }

    public static Performance testVertiBayesFullDataSet() throws Exception {
        initNodes();
        Instances fullData = readData(LABEL, TEST_FULL);
        Performance p = buildAndValidate(FIRSTHALF, SECONDHALF,
                                         readData(LABEL, TEST_FULL), LABEL,
                                         TEST_FULL.replace("Weka.arff",
                                                           ".csv"), NODES, MINPERCENTAGE, fullData);

        //the AIC here is the full AIC
        p.setFullAIC(p.getAIC());
        p.setAIC(0);
        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(p.getRealAuc(), 0.98, AVERAGERROR);
        return p;
    }

    public static Performance testVertiBayesFullDataSetMissing(double treshold) throws Exception {
        initNodes();
        List<WebNode> nodes = buildIrisNetworkBinnedMissing();

        String first = FIRSTHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));

        Instances fullData = readData(LABEL, full);

        Performance p = buildAndValidate(first, second,
                                         fullData, LABEL,
                                         full.replace(".arff",
                                                      ".csv"), nodes, MINPERCENTAGE, fullData);
        double auc = p.getRealAuc();
        //the AIC here is the full AIC
        p.setFullAIC(p.getAIC());
        p.setAIC(0);

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        // check AUC depending on the degree of missing data
        if (treshold == 0.05) {
            assertEquals(auc, 0.97, AVERAGERROR);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.97, AVERAGERROR);
        } else if (treshold == 0.3) {
            assertEquals(auc, 0.90, AVERAGERROR);
        }
        return p;
    }

    private static String generateMissingFullPath(double treshold) {
        return TEST_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
    }
}
