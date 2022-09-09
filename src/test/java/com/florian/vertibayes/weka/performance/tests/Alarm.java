package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;
import com.florian.vertibayes.weka.performance.tests.util.Performance;
import com.florian.vertibayes.weka.performance.tests.util.Variance;
import weka.core.Instances;

import java.io.IOException;
import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildAlarmNetwork;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.averagePerformance;
import static com.florian.vertibayes.weka.performance.tests.util.Performance.checkVariance;
import static com.florian.vertibayes.weka.performance.tests.util.Util.readData;
import static com.florian.vertibayes.weka.performance.tests.util.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.tests.util.WekaPerformance.wekaGenerateErrors;
import static com.florian.vertibayes.weka.performance.tests.util.WekaPerformance.wekaTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Alarm {

    public static final String TEST_FULL = "resources/Experiments/alarm/ALARM10KWEKA.arff";
    public static final String TEST_FULL_MISSING = "resources/Experiments/alarm/alarm10kMissing.arff";

    public static final String FIRSTHALF = "resources/Experiments/alarm/ALARM10k_firsthalf.csv";
    public static final String SECONDHALF = "resources/Experiments/alarm/ALARM10k_secondhalf.csv";

    public static final String FIRSTHALF_MISSING = "resources/Experiments/alarm/alarm10kMissingLeft.csv";
    public static final String SECONDHALF_MISSING = "resources/Experiments/alarm/alarm10kMissingRight.csv";

    public static final String FOLD_LEFTHALF = "resources/Experiments/alarm/folds/alarmLeftSplit";
    public static final String FOLD_RIGHTHALF = "resources/Experiments/alarm/folds/alarmRightSplit";

    public static final String FOLD_LEFTHALF_MISSING = "resources/Experiments/alarm/folds/alarmmissingLeftSplit";
    public static final String FOLD_RIGHTHALF_MISSING = "resources/Experiments/alarm/folds" +
            "/alarmmissingRightSplit";
    public static final String TEST_FOLD = "resources/Experiments/alarm/folds/alarm";

    public static final String ALARM_WEKA_BIF = "resources/Experiments/alarm/alarmbif.xml";

    private static final String LABEL = "BP";
    private static List<WebNode> NODES = buildAlarmNetwork();
    private static final String NAME = "Alarm";

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

    public Alarm() throws IOException {
    }

    private static void initNodes() {
        NODES = buildAlarmNetwork();
    }

    public static Performance kFoldUnknown(double treshold) throws Exception {
        initNodes();
        String full = generateMissingFullPath(treshold);
        Instances fullData = readData(LABEL, full);
        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING, TEST_FOLD,
                                                                         LABEL, NODES, MINPERCENTAGE, fullData);
        List<Performance> performances = test.kFoldTest(treshold);
        Performance p = averagePerformance(performances);
        checkVariance(performances, p, FOLDVARIANCEMISSING);

        p.setName(NAME);
        Performance alarmUnknown = Alarm.weka(treshold);
        p.setWekaAuc(alarmUnknown.getWekaAuc());
        p.setWekaErrors(alarmUnknown.getWekaErrors());
        p.setWekaAIC(alarmUnknown.getWekaAIC());
        p.matchErrors();

        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.88, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.88, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.88, AVERAGERROR);
        } else if (treshold == 0.1) {
            assertEquals(p.getRealAuc(), 0.84, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.84, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.84, AVERAGERROR);
        } else if (treshold == 0.3) {
            assertEquals(p.getRealAuc(), 0.75, AVERAGERROR);
            assertEquals(p.getSyntheticAuc(), 0.75, AVERAGERROR);
            assertEquals(p.getSyntheticFoldAuc(), 0.75, AVERAGERROR);
        }

        assertEquals(p.getWekaAuc(), p.getRealAuc(), AVERAGERROR);
        assertEquals(p.getWekaAuc(), p.getSyntheticAuc(), AVERAGERROR);
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
        Performance alarmWeka = Alarm.weka();
        p.setWekaAuc(alarmWeka.getWekaAuc());
        p.setWekaErrors(alarmWeka.getWekaErrors());
        p.setWekaAIC(alarmWeka.getWekaAIC());
        p.matchErrors();

        assertEquals(p.getRealAuc(), 0.91, AVERAGERROR);
        assertEquals(p.getSyntheticAuc(), 0.91, AVERAGERROR);
        assertEquals(p.getSyntheticFoldAuc(), 0.91, AVERAGERROR);

        assertEquals(p.getWekaAuc(), p.getRealAuc(), AVERAGERROR);
        assertEquals(p.getWekaAuc(), p.getSyntheticAuc(), AVERAGERROR);
        assertEquals(p.getWekaAuc(), p.getSyntheticFoldAuc(), AVERAGERROR);

        return p;
    }

    private static Performance weka(double treshold) throws Exception {
        String testFold = TEST_FOLD + "Treshold" + String.valueOf(treshold).replace(".", "_") + "missing.arff";

        Performance res = new Performance();
        Performance errors = wekaGenerateErrors(LABEL,
                                                ALARM_WEKA_BIF.replace("Missing",
                                                                       "Treshold" + String.valueOf(treshold)
                                                                               .replace(".", "_")),
                                                testFold);
        res.getWekaErrors().putAll(errors.getWekaErrors());

        res.setWekaAuc(wekaTest(LABEL,
                                ALARM_WEKA_BIF.replace("Missing",
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
        Performance errors = wekaGenerateErrors(LABEL, ALARM_WEKA_BIF, testFold);
        res.getWekaErrors().putAll(errors.getWekaErrors());
        res.setWekaAuc(wekaTest(LABEL, ALARM_WEKA_BIF, TEST_FULL, res));
        return res;
    }

    public static Performance testVertiBayesFullDataSet() throws Exception {
        initNodes();
        Instances fullData = readData(LABEL, TEST_FULL);
        Performance p = buildAndValidate(FIRSTHALF, SECONDHALF, fullData,
                                         LABEL, TEST_FULL.replace("WEKA.arff", ".csv"),
                                         NODES, MINPERCENTAGE, fullData);
        double auc = p.getRealAuc();


        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.92, AVERAGERROR);
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
        Instances fulldata = readData(LABEL, full);
        Performance p = buildAndValidate(first, second,
                                         fulldata, LABEL, full.replace(".arff",
                                                                       ".csv"), NODES,
                                         MINPERCENTAGE, fulldata);
        double auc = p.getRealAuc();
        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.88, AVERAGERROR);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.84, AVERAGERROR);
        } else if (treshold == 0.3) {
            assertEquals(auc, 0.75, AVERAGERROR);
        }
        return p;
    }

    private static String generateMissingFullPath(double treshold) {
        return TEST_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
    }
}
