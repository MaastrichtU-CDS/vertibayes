package com.florian.vertibayes.weka;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationResponse;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationTestResponse;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import org.junit.jupiter.api.Test;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPerformance {
    private int FOLDS = 10;

    public static final String IRIS_WEKA_BIF = "resources/Experiments/iris/irisWekaBif.xml";
    public static final String ALARM_WEKA_BIF = "resources/Experiments/alarm/alarmbif.xml";
    public static final String ASIA_WEKA_BIF = "resources/Experiments/asia/asiabif.xml";

    public static final String FOLD_LEFTHALF_IRIS = "resources/Experiments/iris/folds/irisLeftSplit";
    public static final String TEST_FOLD_IRIS = "resources/Experiments/iris/folds/iris";
    public static final String FOLD_RIGHTHALF_IRIS = "resources/Experiments/iris/folds/irisRightSplit";
    public static final String FOLD_LEFTHALF_IRIS_MISSING = "resources/Experiments/iris/folds/irismissingLeftSplit";
    public static final String FOLD_RIGHTHALF_IRIS_MISSING = "resources/Experiments/iris/folds/irismissingRightSplit";

    public static final String FOLD_LEFTHALF_ALARM = "resources/Experiments/alarm/folds/alarmLeftSplit";
    public static final String TEST_FOLD_ALARM = "resources/Experiments/alarm/folds/alarm";
    public static final String FOLD_RIGHTHALF_ALARM = "resources/Experiments/alarm/folds/alarmRightSplit";
    public static final String FOLD_LEFTHALF_ALARM_MISSING = "resources/Experiments/alarm/folds/alarmmissingLeftSplit";
    public static final String FOLD_RIGHTHALF_ALARM_MISSING = "resources/Experiments/alarm/folds" +
            "/alarmmissingRightSplit";

    public static final String FOLD_LEFTHALF_ASIA = "resources/Experiments/asia/folds/asiaLeftSplit";
    public static final String TEST_FOLD_ASIA = "resources/Experiments/asia/folds/asia";
    public static final String FOLD_RIGHTHALF_ASIA = "resources/Experiments/asia/folds/asiaRightSplit";
    public static final String FOLD_LEFTHALF_ASIA_MISSING = "resources/Experiments/asia/folds/asiamissingLeftSplit";
    public static final String FOLD_RIGHTHALF_ASIA_MISSING = "resources/Experiments/asia/folds" +
            "/asiamissingRightSplit";

    public static final String TEST_IRIS_FULL = "resources/Experiments/iris/irisWeka.arff";
    public static final String FIRSTHALF_IRIS = "resources/Experiments/iris/iris_firsthalf.csv";
    public static final String SECONDHALF_IRIS = "resources/Experiments/iris/iris_secondhalf.csv";

    public static final String TEST_ALARM_FULL = "resources/Experiments/alarm/ALARM10KWEKA.arff";
    public static final String FIRSTHALF_ALARM = "resources/Experiments/alarm/ALARM10k_firsthalf.csv";
    public static final String SECONDHALF_ALARM = "resources/Experiments/alarm/ALARM10k_secondhalf.csv";

    public static final String TEST_ASIA_FULL = "resources/Experiments/asia/Asia10KWEKA.arff";
    public static final String FIRSTHALF_ASIA = "resources/Experiments/asia/Asia10k_firsthalf.csv";
    public static final String SECONDHALF_ASIA = "resources/Experiments/asia/Asia10k_secondhalf.csv";

    public static final String TEST_IRIS_FULL_MISSING = "resources/Experiments/iris/irisMissing.arff";
    public static final String FIRSTHALF_IRIS_MISSING = "resources/Experiments/iris/irisMissingLeft.csv";
    public static final String SECONDHALF_IRIS_MISSING = "resources/Experiments/iris/irisMissingRight.csv";

    public static final String TEST_ALARM_FULL_MISSING = "resources/Experiments/alarm/alarm10kMissing.arff";
    public static final String FIRSTHALF_ALARM_MISSING = "resources/Experiments/alarm/alarm10kMissingLeft.csv";
    public static final String SECONDHALF_ALARM_MISSING = "resources/Experiments/alarm/alarm10kMissingRight.csv";

    public static final String TEST_ASIA_FULL_MISSING = "resources/Experiments/asia/asia10kMissing.arff";
    public static final String FIRSTHALF_ASIA_MISSING = "resources/Experiments/asia/asia10kMissingLeft.csv";
    public static final String SECONDHALF_ASIA_MISSING = "resources/Experiments/asia/asia10kMissingRight.csv";

    private static final List<Double> TRESHHOLDS = Arrays.asList(0.05, 0.1);
    private static final boolean SMALL_TEST = true;

    // IMPORTANT TO NOTE; IF THESE TEST BEHAVE WEIRDLY MANUALLY CHECK IN WEKA.
    // ISSUES LIKE MISALIGNED COLLUMNS LEAD TO WEIRD RESULTS WEKA WILL SHOW THIS BY THROWING AN ERROR
    // TO TEST MANUALLY USE TEST.ARFF THAT IS GENERATED DURING THESE TEST CASES AS THE BASELINE THEN COMPARE TO
    // THE CORRESPONDING TEST FILE

    // ALARM TAKES ABOUT 5 MINUTES TO TRAIN ONCE
    // IRIS ABOUT 1.5
    // ASIA ABOUT 20 seconds
    // MISSING DATA ARE EVEN SLOWER DUE TO THE EXTRA CPD'S THAT NEED TO BE CALCULATED (ALARM GOES TO 11)
    // BOTTLENECK IS POSED BY NUMBER OF VALUES IN CPD WHICH IS DEPENDEND ON THE NUMBER OF UNIQUE ATTRIBUTE VALUES
    // (OR BINS) AS WELL AS THE AMOUNT OF PARENT-CHILD RELATIONSHIPS (E.G. 1 PARENT WITH 2 VALUES FOR A CHILD WITH 2
    // VALUES = 4 VALUES IN THE CPD, 2 PARENTS WITH 2 VALUES = 8 VALUES IN THE CPD)

    // FULL K-FOLD TESTCASE WILL TAKE HOURS

    @Test
    public void testVertiBayesKFoldKnown() throws Exception {
        // Federated and non federated variants should have comparable performance
        // Due to the inherent randomness it is possible for the federated setup to outperform the normal setup

        // Full Asia K-fold will take ~6 minutes
        // Full Iris K-fold will take ~33 minutes
        // Full ALARM K-fold will take ~3 hours
        // Do not start IRIS or ALARM unless you want to wait

        List<Integer> folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
        long start = System.currentTimeMillis();

        PerformanceTuple asiaFed = asia(folds);
        double asia = wekaTest("lung", ASIA_WEKA_BIF, TEST_ASIA_FULL);

        assertEquals(asia, asiaFed.getRealAuc(), 0.025);
        assertEquals(asia, asiaFed.getSyntheticAuc(), 0.025);

        System.out.println("Asia :" + asia);
        System.out.println("Validating against real data:");
        System.out.println("Federated");
        System.out.println("Asia :" + asiaFed.getRealAuc());

        System.out.println("Validating against synthetic data:");
        System.out.println("Federated");
        System.out.println("Asia :" + asiaFed.getSyntheticAuc());

        System.out.println("Time: " + (System.currentTimeMillis() - start));
        if (!SMALL_TEST) {
            start = System.currentTimeMillis();


            PerformanceTuple irisAutomaticFed = irisAutomatic(folds);
            double irisAutomatic = wekaTest("label", IRIS_WEKA_BIF, TEST_IRIS_FULL);

            assertEquals(irisAutomatic, irisAutomaticFed.getRealAuc(), 0.025);
            assertEquals(irisAutomatic, irisAutomaticFed.getSyntheticAuc(), 0.025);

            System.out.println("IrisAutomatic :" + irisAutomatic);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("IrisAutomatic :" + irisAutomaticFed.getRealAuc());

            System.out.println("Validating against synthetic data:");
            System.out.println("Federated");
            System.out.println("IrisAutomatic :" + irisAutomaticFed.getSyntheticAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();


            PerformanceTuple irisManualFed = irisManual(folds);
            double irisManual = wekaTest("label", IRIS_WEKA_BIF, TEST_IRIS_FULL);

            assertEquals(irisManual, irisManualFed.getRealAuc(), 0.025);
            assertEquals(irisManual, irisManualFed.getSyntheticAuc(), 0.025);

            System.out.println("IrisManual :" + irisManual);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("IrisManual :" + irisManualFed.getRealAuc());

            System.out.println("Validating against synthetic data:");
            System.out.println("Federated");
            System.out.println("IrisManual :" + irisManualFed.getSyntheticAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();

            PerformanceTuple alarmFed = alarm(folds);
            double alarm = wekaTest("BP", ALARM_WEKA_BIF, TEST_ALARM_FULL);

            assertEquals(alarm, alarmFed.getRealAuc(), 0.025);
            assertEquals(alarm, alarmFed.getSyntheticAuc(), 0.025);

            System.out.println("Alarm :" + alarm);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("Alarm :" + alarmFed.getRealAuc());

            System.out.println("Validating against synthetic data:");
            System.out.println("Federated");
            System.out.println("Alarm :" + alarmFed.getSyntheticAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));
        }
    }

    @Test
    public void testVertiBayesKFoldUnKnown() throws Exception {
        // Federated and non federated variants should have comparable performance
        // Due to the inherent randomness it is possible for the federated setup to outperform the normal setup

        // Full Asia K-fold will take ~6 minutes
        // Full Iris K-fold will take ~33 minutes
        // Full ALARM K-fold will take ~3 hours
        // Do not start IRIS or ALARM unless you want to wait

        List<Integer> folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
        for (double treshold : TRESHHOLDS) {

            System.out.println("Treshold: " + treshold);
            long start = System.currentTimeMillis();

            PerformanceTuple asiaUnknownFed = asiaUnknown(folds, treshold);
            double asiaUnknown = wekaTest("lung",
                                          ASIA_WEKA_BIF.replace("Missing",
                                                                "Treshold" + String.valueOf(treshold)
                                                                        .replace(".", "_")),
                                          TEST_ASIA_FULL_MISSING.replace("Missing",
                                                                         "MissingTreshold" + String.valueOf(treshold)
                                                                                 .replace(".", "_")));

            assertEquals(asiaUnknown, asiaUnknownFed.getRealAuc(), 0.025);
            assertEquals(asiaUnknown, asiaUnknownFed.getSyntheticAuc(), 0.025);

            System.out.println("Asia unknown :" + asiaUnknown);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("Asia unknown :" + asiaUnknownFed.getRealAuc());

            System.out.println("Validating against synthetic data:");
            System.out.println("Federated");
            System.out.println("Asia unknown :" + asiaUnknownFed.getSyntheticAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();

            PerformanceTuple irisAutomaticUnknownFed = irisUnknown(folds, treshold, true);
            double irisAutomaticUnknown = wekaTest("label",
                                                   IRIS_WEKA_BIF.replace("Missing",
                                                                         "MissingTreshold" + String.valueOf(
                                                                                         treshold)
                                                                                 .replace(".", "_"))
                    , TEST_IRIS_FULL_MISSING.replace("Missing",
                                                     "MissingTreshold" + String.valueOf(treshold)
                                                             .replace(".", "_")));

            assertEquals(irisAutomaticUnknown, irisAutomaticUnknownFed.getRealAuc(), 0.025);
            assertEquals(irisAutomaticUnknown, irisAutomaticUnknownFed.getSyntheticAuc(), 0.025);

            System.out.println("IrisAutomatic unknown :" + irisAutomaticUnknown);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("IrisAutomatic unknown :" + irisAutomaticUnknownFed.getRealAuc());

            System.out.println("Validating against synthetic data:");
            System.out.println("Federated");
            System.out.println("IrisAutomatic unknown :" + irisAutomaticUnknownFed.getSyntheticAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();

            if (!SMALL_TEST) {
                PerformanceTuple irisManualUnknownFed = irisUnknown(folds, treshold, false);
                double irisManualUnknown = wekaTest("label",
                                                    IRIS_WEKA_BIF.replace("Missing",
                                                                          "MissingTreshold" + String.valueOf(treshold)
                                                                                  .replace(".", "_")),
                                                    TEST_IRIS_FULL_MISSING.replace(
                                                            "Missing", "MissingTreshold" + String.valueOf(treshold)
                                                                    .replace(".", "_")));

                assertEquals(irisManualUnknown, irisManualUnknownFed.getRealAuc(), 0.025);
                assertEquals(irisManualUnknown, irisManualUnknownFed.getSyntheticAuc(), 0.025);

                System.out.println("IrisManual unknown :" + irisManualUnknown);
                System.out.println("Validating against real data:");
                System.out.println("Federated");
                System.out.println("IrisManual unknown :" + irisManualUnknownFed.getRealAuc());

                System.out.println("Validating against synthetic data:");
                System.out.println("Federated");
                System.out.println("IrisManual unknown :" + irisManualUnknownFed.getSyntheticAuc());

                System.out.println("Time: " + (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();

                PerformanceTuple alarmUnknownFed = alarmUnknown(folds, treshold);
                double alarmUnknown = wekaTest("BP",
                                               ALARM_WEKA_BIF.replace("Missing",
                                                                      "MissingTreshold" + String.valueOf(treshold)
                                                                              .replace(".", "_")),
                                               TEST_ALARM_FULL_MISSING.replace(
                                                       "Missing", "MissingTreshold" + String.valueOf(treshold)
                                                               .replace(".", "_")));

                assertEquals(alarmUnknown, alarmUnknownFed.getRealAuc(), 0.025);
                assertEquals(alarmUnknown, alarmUnknownFed.getSyntheticAuc(), 0.025);

                System.out.println("Alarm unknown :" + alarmUnknown);
                System.out.println("Validating against real data:");
                System.out.println("Federated");
                System.out.println("Alarm unknown :" + alarmUnknownFed.getRealAuc());

                System.out.println("Validating against synthetic data:");
                System.out.println("Federated");
                System.out.println("Alarm unknown :" + alarmUnknownFed.getSyntheticAuc());

                System.out.println("Time: " + (System.currentTimeMillis() - start));
            }
        }
    }

    @Test
    public void testVertiBayesNoFold() throws Exception {
        long start = System.currentTimeMillis();
        testVertiBayesFullDataSetIris();
        System.out.println("Time: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        testVertiBayesFullDataSetAsia();
        System.out.println("Time: " + (System.currentTimeMillis() - start));


        // do not turn on ALARM unless you have the time to wait
        if (!SMALL_TEST) {
            start = System.currentTimeMillis();
            testVertiBayesFullDataSetAlarm();
            System.out.println("Time: " + (System.currentTimeMillis() - start));
        }
        for (double d : TRESHHOLDS) {
            start = System.currentTimeMillis();
            testVertiBayesFullDataSetMissingAsia(d);
            System.out.println("Time: " + (System.currentTimeMillis() - start));

            if (!SMALL_TEST) {
                // do not turn on unless you have the time to wait
                start = System.currentTimeMillis();
                testVertiBayesFullDataSetMissingIris(d);
                System.out.println("Time: " + (System.currentTimeMillis() - start));

                start = System.currentTimeMillis();
                testVertiBayesFullDataSetMissingAlarm(d);
                System.out.println("Time: " + (System.currentTimeMillis() - start));
            }
        }
    }

    private void testVertiBayesFullDataSetIris() throws Exception {
        double aucManual = vertiBayesIrisManualBinningTest(FIRSTHALF_IRIS, SECONDHALF_IRIS,
                                                           readData("label", TEST_IRIS_FULL),
                                                           "label").getRealAuc();

        double aucAutomatic = vertiBayesIrisAutomaticBinningTest(FIRSTHALF_IRIS, SECONDHALF_IRIS,
                                                                 readData("label", TEST_IRIS_FULL),
                                                                 "label").getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(aucAutomatic, 0.98, 0.025);
        assertEquals(aucManual, 0.98, 0.025);
    }

    private void testVertiBayesFullDataSetMissingIris(double treshold) throws Exception {
        String first = FIRSTHALF_IRIS_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_IRIS_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_IRIS_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        double auc = vertiBayesIrisMissingTest(first, second,
                                               readData("label", full), "label").getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        // check AUC depending on the degree of missing data
        if (treshold == 0.05) {
            assertEquals(auc, 0.98, 0.025);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.98, 0.025);
        }
    }

    private void testVertiBayesFullDataSetAlarm() throws Exception {
        double auc = vertiBayesAlarmTest(FIRSTHALF_ALARM, SECONDHALF_ALARM, readData("BP", TEST_ALARM_FULL),
                                         "BP").getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.91, 0.025);
    }


    private void testVertiBayesFullDataSetMissingAlarm(double treshold) throws Exception {
        String first = FIRSTHALF_ALARM_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_ALARM_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_ALARM_FULL.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        double auc = vertiBayesAlarmTest(first, second,
                                         readData("BP", full), "BP").getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.88, 0.04);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.88, 0.04);
        }
    }


    private void testVertiBayesFullDataSetAsia() throws Exception {
        double auc = vertiBayesAsiaTest(FIRSTHALF_ASIA, SECONDHALF_ASIA, readData("lung", TEST_ASIA_FULL),
                                        "lung").getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.98, 0.025);
    }


    private void testVertiBayesFullDataSetMissingAsia(double treshold) throws Exception {
        String first = FIRSTHALF_ASIA_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_ASIA_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_ASIA_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        double auc = vertiBayesAsiaTest(first, second,
                                        readData("lung", full), "lung").getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.78, 0.025);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.70, 0.025);
        }
    }

    private PerformanceTuple asia(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ASIA + ids + ".csv";
            String right = FOLD_RIGHTHALF_ASIA + ids + ".csv";
            PerformanceTuple res = vertiBayesAsiaTest(left, right,
                                                      readData("lung", TEST_FOLD_ASIA + fold + "WEKA.arff"),
                                                      "lung");
            assertEquals(res.getRealAuc(), 0.96, 0.05);
            assertEquals(res.getSyntheticAuc(), 0.96, 0.05);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        assertEquals(averageAUC, 0.99, 0.025);
        assertEquals(averageAUCSynthetic, 0.99, 0.025);
        PerformanceTuple tuple = new PerformanceTuple();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        return tuple;
    }

    private PerformanceTuple asiaUnknown(List<Integer> folds, double treshold) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        //unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ASIA_MISSING.replace("missing",
                                                             "Treshold" + String.valueOf(treshold)
                                                                     .replace(".", "_") + "missing") + ids + ".csv";
            String right = FOLD_RIGHTHALF_ASIA_MISSING.replace("missing",
                                                               "Treshold" + String.valueOf(treshold)
                                                                       .replace(".", "_") + "missing") + ids + ".csv";
            PerformanceTuple res = vertiBayesAsiaTest(left, right,
                                                      readData("lung", TEST_FOLD_ASIA +
                                                              "Treshold" + String.valueOf(treshold)
                                                              .replace(".", "_") + "missing" + fold + "WEKA.arff"),
                                                      "lung");
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //The average is still going to be quite close to .78 though
            if (treshold == 0.05) {
                assertEquals(res.getRealAuc(), 0.78, 0.07);
                assertEquals(res.getSyntheticAuc(), 0.78, 0.07);
            } else if (treshold == 0.1) {
                assertEquals(res.getRealAuc(), 0.70, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.70, 0.1);
            }
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        if (treshold == 0.05) {
            assertEquals(averageAUC, 0.78, 0.04);
            assertEquals(averageAUCSynthetic, 0.78, 0.04);
        } else if (treshold == 0.1) {
            assertEquals(averageAUC, 0.7, 0.04);
            assertEquals(averageAUCSynthetic, 0.7, 0.04);
        }
        PerformanceTuple tuple = new PerformanceTuple();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        return tuple;
    }


    private PerformanceTuple alarm(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ALARM + ids + ".csv";
            String right = FOLD_RIGHTHALF_ALARM + ids + ".csv";
            PerformanceTuple res = vertiBayesAlarmTest(left, right,
                                                       readData("BP", TEST_FOLD_ALARM + fold + "WEKA.arff"),
                                                       "BP");
            assertEquals(res.getRealAuc(), 0.91, 0.025);
            assertEquals(res.getSyntheticAuc(), 0.91, 0.025);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        assertEquals(averageAUC, 0.91, 0.025);
        assertEquals(averageAUCSynthetic, 0.91, 0.025);
        PerformanceTuple tuple = new PerformanceTuple();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        return tuple;
    }

    private PerformanceTuple alarmUnknown(List<Integer> folds, double treshold) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        //unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ALARM_MISSING.replace("missing",
                                                              "Treshold" + String.valueOf(treshold)
                                                                      .replace(".", "_") + "missing") + ids + ".csv";
            String right = FOLD_RIGHTHALF_ALARM_MISSING.replace("missing",
                                                                "Treshold" + String.valueOf(treshold)
                                                                        .replace(".", "_") + "missing") + ids + ".csv";
            PerformanceTuple res = vertiBayesAlarmTest(left, right,
                                                       readData("BP",
                                                                TEST_FOLD_ALARM + "Treshold" + String.valueOf(treshold)
                                                                        .replace(".",
                                                                                 "_") + "missing" + fold + "WEKA.arff"),
                                                       "BP");
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //The average is still going to be quite close to .88 though
            if (treshold == 0.05) {
                assertEquals(res.getRealAuc(), 0.88, 0.025);
                assertEquals(res.getSyntheticAuc(), 0.88, 0.025);
            } else if (treshold == 0.1) {
                assertEquals(res.getRealAuc(), 0.88, 0.04);
                assertEquals(res.getSyntheticAuc(), 0.88, 0.04);
            }
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        if (treshold == 0.05) {
            assertEquals(averageAUC, 0.88, 0.025);
            assertEquals(averageAUCSynthetic, 0.88, 0.025);
        } else if (treshold == 0.1) {
            assertEquals(averageAUC, 0.88, 0.025);
            assertEquals(averageAUCSynthetic, 0.88, 0.025);
        }

        PerformanceTuple tuple = new PerformanceTuple();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        return tuple;
    }

    private PerformanceTuple irisAutomatic(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_IRIS + ids + ".csv";
            String right = FOLD_RIGHTHALF_IRIS + ids + ".csv";
            PerformanceTuple res = vertiBayesIrisAutomaticBinningTest(left, right,
                                                                      readData("label",
                                                                               TEST_FOLD_IRIS + fold + "WEKA.arff"),
                                                                      "label");
            assertEquals(res.getRealAuc(), 0.96, 0.05);
            assertEquals(res.getSyntheticAuc(), 0.96, 0.05);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        assertEquals(averageAUC, 0.96, 0.05);
        assertEquals(averageAUCSynthetic, 0.96, 0.05);
        PerformanceTuple tuple = new PerformanceTuple();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        return tuple;
    }

    private PerformanceTuple irisManual(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_IRIS + ids + ".csv";
            String right = FOLD_RIGHTHALF_IRIS + ids + ".csv";
            PerformanceTuple res = vertiBayesIrisManualBinningTest(left, right,
                                                                   readData("label",
                                                                            TEST_FOLD_IRIS + fold + "WEKA.arff"),
                                                                   "label");
            assertEquals(res.getRealAuc(), 0.96, 0.05);
            assertEquals(res.getSyntheticAuc(), 0.96, 0.05);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        assertEquals(averageAUC, 0.96, 0.05);
        assertEquals(averageAUCSynthetic, 0.96, 0.05);
        PerformanceTuple tuple = new PerformanceTuple();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        return tuple;
    }

    private PerformanceTuple irisUnknown(List<Integer> folds, double treshold, boolean automaticBinning)
            throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        //unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_IRIS_MISSING.replace("missing",
                                                             "Treshold" + String.valueOf(treshold)
                                                                     .replace(".", "_") + "missing") + ids + ".csv";
            String right = FOLD_RIGHTHALF_IRIS_MISSING.replace("missing",
                                                               "Treshold" + String.valueOf(treshold)
                                                                       .replace(".", "_") + "missing") + ids + ".csv";
            PerformanceTuple res = null;
            if (automaticBinning) {
                res = vertiBayesIrisMissingAutomaticBinTest(left, right,
                                                            readData("label",
                                                                     TEST_FOLD_IRIS + "Treshold" + String.valueOf(
                                                                             treshold).replace(".", "_") +
                                                                             "missing" + fold + "WEKA.arff"),
                                                            "label");
            } else {
                res = vertiBayesIrisMissingTest(left, right,
                                                readData("label",
                                                         TEST_FOLD_IRIS + "Treshold" + String.valueOf(
                                                                 treshold).replace(".", "_") +
                                                                 "missing" + fold + "WEKA.arff"),
                                                "label");
            }
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //Hence the wide range
            //The average is still going to be quite close to .96 though
            if (treshold == 0.05) {
                assertEquals(res.getRealAuc(), 0.90, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.90, 0.1);
            } else if (treshold == 0.1) {
                assertEquals(res.getRealAuc(), 0.90, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.90, 0.1);
            }
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        if (treshold == 0.05) {
            assertEquals(averageAUC, 0.96, 0.04);
            assertEquals(averageAUCSynthetic, 0.96, 0.04);
        } else if (treshold == 0.1) {
            assertEquals(averageAUC, 0.96, 0.04);
            assertEquals(averageAUCSynthetic, 0.96, 0.04);
        }

        PerformanceTuple tuple = new PerformanceTuple();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        return tuple;
    }

    private PerformanceTuple vertiBayesAsiaTest(String left, String right, Instances testData, String target)
            throws Exception {
        ExpectationMaximizationTestResponse response = (ExpectationMaximizationTestResponse) generateModel(
                buildAsiaNetwork(), left, right, target);
        BayesNet network = response.getWeka();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);
        PerformanceTuple res = new PerformanceTuple();
        res.setSyntheticAuc(response.getSyntheticAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        return res;
    }

    private PerformanceTuple vertiBayesAlarmTest(String left, String right, Instances testData, String target)
            throws Exception {
        ExpectationMaximizationTestResponse response = (ExpectationMaximizationTestResponse) generateModel(
                buildAlarmNetwork(), left, right, target);
        BayesNet network = response.getWeka();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);
        PerformanceTuple res = new PerformanceTuple();
        res.setSyntheticAuc(response.getSyntheticAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        return res;
    }

    private PerformanceTuple vertiBayesIrisManualBinningTest(String left, String right, Instances testData,
                                                             String target)
            throws Exception {
        ExpectationMaximizationTestResponse response = (ExpectationMaximizationTestResponse) generateModel(
                buildIrisNetworkBinned(), left, right, target);
        BayesNet network = response.getWeka();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);
        PerformanceTuple res = new PerformanceTuple();
        res.setSyntheticAuc(response.getSyntheticAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        return res;
    }

    private PerformanceTuple vertiBayesIrisAutomaticBinningTest(String left, String right, Instances testData,
                                                                String target)
            throws Exception {
        ExpectationMaximizationTestResponse response = (ExpectationMaximizationTestResponse) generateModel(
                buildIrisNetworkNoBins(), left, right, target);
        BayesNet network = response.getWeka();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);
        PerformanceTuple res = new PerformanceTuple();
        res.setSyntheticAuc(response.getSyntheticAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        return res;
    }

    private PerformanceTuple vertiBayesIrisMissingTest(String left, String right, Instances testData, String target)
            throws Exception {
        ExpectationMaximizationTestResponse response = (ExpectationMaximizationTestResponse) generateModel(
                buildIrisNetworkBinnedMissing(), left, right, target);
        BayesNet network = response.getWeka();
        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);

        PerformanceTuple res = new PerformanceTuple();
        res.setSyntheticAuc(response.getSyntheticAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        return res;
    }

    private PerformanceTuple vertiBayesIrisMissingAutomaticBinTest(String left, String right, Instances testData,
                                                                   String target)
            throws Exception {
        ExpectationMaximizationTestResponse response = (ExpectationMaximizationTestResponse) generateModel(
                buildIrisNetworkNoBins(), left, right, target);
        BayesNet network = response.getWeka();
        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);

        PerformanceTuple res = new PerformanceTuple();
        res.setSyntheticAuc(response.getSyntheticAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        return res;
    }

    private ExpectationMaximizationResponse generateModel(List<WebNode> input, String firsthalf, String secondhalf,
                                                          String target)
            throws Exception {
        VertiBayesCentralServer central = createCentral(firsthalf, secondhalf);
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(input);
        req.setTarget(target);
        return central.expectationMaximization(req);
    }


    private double wekaTest(String target, String biff, String arff) throws Exception {
        FromFile search = new FromFile();
        search.setBIFFile(biff);

        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = readData(target, arff);

        Evaluation eval = new Evaluation(data);
        network.buildClassifier(data);


        eval.crossValidateModel(network, data, 10, new Random(1));

        return eval.weightedAreaUnderROC();

    }

    private Instances readDataCSV(String target, String csv) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csv));
        Instances data = loader.getDataSet();

        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).name().equals(target)) {
                data.setClassIndex(i);
                break;
            }
        }
        return data;
    }

    private Instances readData(String target, String arff) throws IOException {
        Instances data = new Instances(
                new BufferedReader(new FileReader(arff)));
        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).name().equals(target)) {
                data.setClassIndex(i);
                break;
            }
        }
        return data;
    }

    private static VertiBayesCentralServer createCentral(String firsthalf, String secondhalf) {
        BayesServer station1 = new BayesServer(firsthalf, "1");
        BayesServer station2 = new BayesServer(secondhalf, "2");

        VertiBayesEndpoint endpoint1 = new VertiBayesEndpoint(station1);
        VertiBayesEndpoint endpoint2 = new VertiBayesEndpoint(station2);
        BayesServer secret = new BayesServer("3", Arrays.asList(endpoint1, endpoint2));

        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.add(endpoint1);
        all.add(endpoint2);
        all.add(secretEnd);
        secret.setEndpoints(all);
        station1.setEndpoints(all);
        station2.setEndpoints(all);

        VertiBayesCentralServer central = new VertiBayesCentralServer(true);
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2), secretEnd);
        return central;
    }
}
