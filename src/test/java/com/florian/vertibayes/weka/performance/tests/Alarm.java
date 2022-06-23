package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.Performance;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceMissingTestBase;
import com.florian.vertibayes.weka.performance.tests.base.PerformanceTestBase;

import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.buildAlarmNetwork;
import static com.florian.vertibayes.weka.performance.Util.readData;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;
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
    private final static List<WebNode> nodes = buildAlarmNetwork();


    public static Performance kFoldUnknown(double treshold) throws Exception {
        PerformanceMissingTestBase test = new PerformanceMissingTestBase(FOLD_LEFTHALF_MISSING,
                                                                         FOLD_RIGHTHALF_MISSING, TEST_FOLD,
                                                                         LABEL, nodes);
        Performance p = test.kFoldTest(treshold);
        double alarmUnknown = Alarm.weka(treshold);
        p.setWekaAuc(alarmUnknown);
        if (treshold == 0.05) {
            assertEquals(p.getRealAuc(), 0.88, 0.05);
            assertEquals(p.getSyntheticAuc(), 0.88, 0.05);
            assertEquals(p.getSyntheticFoldAuc(), 0.88, 0.05);
        } else if (treshold == 0.1) {
            assertEquals(p.getRealAuc(), 0.80, 0.1);
            assertEquals(p.getSyntheticAuc(), 0.80, 0.1);
            assertEquals(p.getSyntheticFoldAuc(), 0.80, 0.1);
        }

        assertEquals(alarmUnknown, p.getRealAuc(), 0.05);
        assertEquals(alarmUnknown, p.getSyntheticAuc(), 0.05);
        assertEquals(alarmUnknown, p.getSyntheticFoldAuc(), 0.05);

        return p;
    }

    public static Performance kFold() throws Exception {
        PerformanceTestBase test = new PerformanceTestBase(FOLD_LEFTHALF,
                                                           FOLD_RIGHTHALF, TEST_FOLD,
                                                           LABEL, nodes);
        Performance p = test.kFoldTest();
        double alarmWeka = Alarm.weka();
        p.setWekaAuc(alarmWeka);

        assertEquals(p.getRealAuc(), 0.91, 0.025);
        assertEquals(p.getSyntheticAuc(), 0.91, 0.025);
        assertEquals(p.getSyntheticFoldAuc(), 0.91, 0.025);

        assertEquals(alarmWeka, p.getRealAuc(), 0.025);
        assertEquals(alarmWeka, p.getSyntheticAuc(), 0.025);
        assertEquals(alarmWeka, p.getSyntheticFoldAuc(), 0.025);

        return p;
    }

    public static double weka(double treshold) throws Exception {
        return wekaTest(LABEL,
                        ALARM_WEKA_BIF.replace("Missing",
                                               "Treshold" + String.valueOf(treshold)
                                                       .replace(".", "_")),
                        TEST_FULL_MISSING.replace("Missing",
                                                  "MissingTreshold" + String.valueOf(treshold)
                                                          .replace(".", "_")));
    }

    public static double weka() throws Exception {
        return wekaTest("lung", ALARM_WEKA_BIF, TEST_FULL);
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
                                      readData(LABEL, full), LABEL, full.replace("WEKA.arff",
                                                                                 ".csv"), nodes).getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.88, 0.04);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.88, 0.04);
        }
    }
}
