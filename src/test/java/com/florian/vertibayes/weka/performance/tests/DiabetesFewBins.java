package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;
import com.florian.vertibayes.weka.performance.tests.util.Performance;
import com.florian.vertibayes.weka.performance.tests.util.Variance;
import weka.core.Instances;

import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildDiabetesNetwork;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.averagePerformance;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.checkVariance;
import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;
import static com.florian.vertibayes.weka.performance.tests.util.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.tests.util.WekaPerformance.wekaGenerateErrors;
import static com.florian.vertibayes.weka.performance.tests.util.WekaPerformance.wekaTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiabetesFewBins extends Diabetes {

    private static final String LABEL = "Outcome";
    private static List<WebNode> NODES = buildDiabetesNetwork();
    private static final String NAME = "DiabetesFewBins";

    private static final double AVERAGERROR = 0.05;
    private static final Variance FOLDVARIANCE;
    private static final Variance FOLDVARIANCEMISSING;


    private static final double MINPERCENTAGE = 0.25;

    static {
        FOLDVARIANCE = new Variance();
        FOLDVARIANCE.setRealAucVariance(0.15);
        FOLDVARIANCE.setSyntheticAucVariance(0.15);
        FOLDVARIANCE.setSyntheticFoldAucVariance(0.15);

        FOLDVARIANCEMISSING = new Variance();
        FOLDVARIANCEMISSING.setRealAucVariance(0.15);
        FOLDVARIANCEMISSING.setSyntheticAucVariance(0.15);
        FOLDVARIANCEMISSING.setSyntheticFoldAucVariance(0.15);
    }

    private static void initNodes() {
        NODES = buildDiabetesNetwork();
    }

    public static Performance kFoldUnknown(double treshold) throws Exception {
        initNodes();
        String full = generateMissingFullPath(treshold);
        Instances fullData = readData(LABEL, full);
        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING,
                                                                         TEST_FOLD,
                                                                         LABEL, NODES, MINPERCENTAGE, fullData);
        List<Performance> performances = test.kFoldTest(treshold);
        Performance p = averagePerformance(performances);
        checkVariance(performances, p, FOLDVARIANCEMISSING);
        p.setName(NAME);
        Performance diabetesUnknown = DiabetesFewBins.weka(treshold);
        p.setWekaAuc(diabetesUnknown.getWekaAuc());
        p.setWekaErrors(diabetesUnknown.getWekaErrors());
        p.setWekaAIC(diabetesUnknown.getWekaAIC());
        p.matchErrors();

        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.79, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.85, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.80, AVERAGERROR);
        } else if (treshold == 0.1) {
            assertEquals(p.getRealAuc(), 0.75, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.83, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.75, AVERAGERROR);
        } else if (treshold == 0.3) {
            assertEquals(p.getRealAuc(), 0.65, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.70, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.65, AVERAGERROR);
        }

        assertEquals(p.getWekaAuc(), p.getRealAuc(), AVERAGERROR);
        //Using synthetic training data in k-fold results in overfitting for diabetes.
        //Other validation methods have expected results
        assertEquals(p.getWekaAuc(), p.getSyntheticAuc(), 0.11);
        assertEquals(p.getWekaAuc(), p.getSyntheticFoldAuc(), AVERAGERROR);

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
        Performance diabetesWeka = DiabetesFewBins.weka();
        p.setWekaAuc(diabetesWeka.getWekaAuc());
        p.setWekaErrors(diabetesWeka.getWekaErrors());
        p.setWekaAIC(diabetesWeka.getWekaAIC());
        p.matchErrors();

        assertEquals(p.getRealAuc(), 0.79, AVERAGERROR);
        assertEquals(p.getSyntheticAuc(), 0.89, AVERAGERROR);
        assertEquals(p.getSyntheticFoldAuc(), 0.82, AVERAGERROR);
        assertEquals(p.getWekaAuc(), p.getRealAuc(), AVERAGERROR);
        //Using synthetic training data in k-fold results in overfitting for diabetes
        //Other validation methods have expected results
        assertEquals(p.getWekaAuc(), p.getSyntheticAuc(), 0.11);
        assertEquals(p.getWekaAuc(), p.getSyntheticFoldAuc(), AVERAGERROR);
        return p;
    }

    private static Performance weka(double treshold) throws Exception {
        String testFold = TEST_FOLD + "Treshold" + String.valueOf(treshold).replace(".", "_") + "missing.arff";

        Performance res = new Performance();
        Performance errors = wekaGenerateErrors(LABEL,
                                                DIABETES_WEKA_BIF.replace("Missing",
                                                                          "Treshold" + String.valueOf(treshold)
                                                                                  .replace(".", "_")),
                                                testFold);
        res.getWekaErrors().putAll(errors.getWekaErrors());

        res.setWekaAuc(wekaTest(LABEL,
                                DIABETES_WEKA_BIF.replace("Missing",
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
        Performance errors = wekaGenerateErrors(LABEL, DIABETES_WEKA_BIF, testFold);
        res.getWekaErrors().putAll(errors.getWekaErrors());
        res.setWekaAuc(wekaTest(LABEL, DIABETES_WEKA_BIF, TEST_FULL, res));
        return res;
    }

    public static Performance testVertiBayesFullDataSet() throws Exception {
        initNodes();
        Instances fullData = readData(LABEL, TEST_FULL);
        Performance p = buildAndValidate(FIRSTHALF, SECONDHALF,
                                         fullData,
                                         LABEL, TEST_FULL.replace("WEKA.arff", ".csv"), NODES,
                                         MINPERCENTAGE, fullData);

        //the AIC here is the full AIC
        p.setFullAIC(p.getAIC());
        p.setAIC(0);

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(p.getRealAuc(), 0.82, AVERAGERROR);
        return p;
    }

    public static Performance testVertiBayesFullDataSetMissing(double treshold) throws Exception {
        initNodes();
        String first = FIRSTHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));

        Instances fullData = readData(LABEL, full);
        Performance p = buildAndValidate(first, second, fullData, LABEL,
                                         full.replace(".arff", ".csv"), NODES, MINPERCENTAGE, fullData);
        double auc = p.getRealAuc();
        //the AIC here is the full AIC
        p.setFullAIC(p.getAIC());
        p.setAIC(0);

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.82, AVERAGERROR);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.78, AVERAGERROR);
        } else if (treshold == 0.3) {
            assertEquals(auc, 0.70, AVERAGERROR);
        }
        return p;
    }

    private static String generateMissingFullPath(double treshold) {
        return TEST_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
    }
}
