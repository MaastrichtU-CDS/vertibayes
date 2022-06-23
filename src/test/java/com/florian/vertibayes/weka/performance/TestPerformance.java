package com.florian.vertibayes.weka.performance;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.*;
import static com.florian.vertibayes.weka.performance.Util.readData;
import static com.florian.vertibayes.weka.performance.VertiBayesPerformance.buildAndValidate;
import static com.florian.vertibayes.weka.performance.WekaPerformance.wekaTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPerformance {
    private int FOLDS = 10;

    public static final String IRIS_WEKA_BIF = "resources/Experiments/iris/irisWekaBif.xml";
    public static final String ALARM_WEKA_BIF = "resources/Experiments/alarm/alarmbif.xml";
    public static final String ASIA_WEKA_BIF = "resources/Experiments/asia/asiabif.xml";
    public static final String DIABETES_WEKA_BIF = "resources/Experiments/diabetes/diabetesbif.xml";

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

    public static final String FOLD_LEFTHALF_Diabetes = "resources/Experiments/diabetes/folds/diabetesLeftSplit";
    public static final String TEST_FOLD_Diabetes = "resources/Experiments/diabetes/folds/diabetes";
    public static final String FOLD_RIGHTHALF_Diabetes = "resources/Experiments/diabetes/folds/diabetesRightSplit";
    public static final String FOLD_LEFTHALF_Diabetes_MISSING = "resources/Experiments/diabetes/folds" +
            "/diabetesmissingLeftSplit";
    public static final String FOLD_RIGHTHALF_Diabetes_MISSING = "resources/Experiments/diabetes/folds" +
            "/diabetesmissingRightSplit";

    public static final String TEST_IRIS_FULL = "resources/Experiments/iris/irisWeka.arff";
    public static final String FIRSTHALF_IRIS = "resources/Experiments/iris/iris_firsthalf.csv";
    public static final String SECONDHALF_IRIS = "resources/Experiments/iris/iris_secondhalf.csv";

    public static final String TEST_ALARM_FULL = "resources/Experiments/alarm/ALARM10KWEKA.arff";
    public static final String FIRSTHALF_ALARM = "resources/Experiments/alarm/ALARM10k_firsthalf.csv";
    public static final String SECONDHALF_ALARM = "resources/Experiments/alarm/ALARM10k_secondhalf.csv";

    public static final String TEST_ASIA_FULL = "resources/Experiments/asia/Asia10KWEKA.arff";
    public static final String FIRSTHALF_ASIA = "resources/Experiments/asia/Asia10k_firsthalf.csv";
    public static final String SECONDHALF_ASIA = "resources/Experiments/asia/Asia10k_secondhalf.csv";

    public static final String TEST_DIABETES_FULL = "resources/Experiments/diabetes/diabetesWEKA.arff";
    public static final String FIRSTHALF_DIABETES = "resources/Experiments/diabetes/diabetes_firsthalf.csv";
    public static final String SECONDHALF_DIABETES = "resources/Experiments/diabetes/diabetes_secondhalf.csv";

    public static final String TEST_IRIS_FULL_MISSING = "resources/Experiments/iris/irisMissing.arff";
    public static final String FIRSTHALF_IRIS_MISSING = "resources/Experiments/iris/irisMissingLeft.csv";
    public static final String SECONDHALF_IRIS_MISSING = "resources/Experiments/iris/irisMissingRight.csv";

    public static final String TEST_ALARM_FULL_MISSING = "resources/Experiments/alarm/alarm10kMissing.arff";
    public static final String FIRSTHALF_ALARM_MISSING = "resources/Experiments/alarm/alarm10kMissingLeft.csv";
    public static final String SECONDHALF_ALARM_MISSING = "resources/Experiments/alarm/alarm10kMissingRight.csv";

    public static final String TEST_ASIA_FULL_MISSING = "resources/Experiments/asia/asia10kMissing.arff";
    public static final String FIRSTHALF_ASIA_MISSING = "resources/Experiments/asia/asia10kMissingLeft.csv";
    public static final String SECONDHALF_ASIA_MISSING = "resources/Experiments/asia/asia10kMissingRight.csv";

    public static final String TEST_DIABETES_FULL_MISSING = "resources/Experiments/diabetes/diabetesMissing.arff";
    public static final String FIRSTHALF_DIABETES_MISSING = "resources/Experiments/diabetes/diabetesMissingLeft.csv";
    public static final String SECONDHALF_DIABETES_MISSING = "resources/Experiments/diabetes/diabetesMissingRight.csv";

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

    // FULL K-FOLD TESTCASE WILL TAKE 7-8 hours
    // SKIPPING SYNTHETIC FOLD TESTING WILL CUT THE TIME IN HALF

    // IMPORTANT NOTE 2: DUE TO THE RANDOM NATURE OF EM,DATA GENERATION & THE FOLDS IT IS POSSIBLE TO GET THE
    // OCCASIONAL TERRIBLE PERFORMANCE, ESPECIALLY ON INDIVIDUAL FOLDS. RERUN THE TEST AND SEE IF IT HAPPENS AGAIN.

    @Test
    public void testVertiBayesDiabetes() throws Exception {
        List<Integer> folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }

        List<Double> tresh = new ArrayList<>();
        tresh.addAll(TRESHHOLDS);
        tresh.add(0.3);
        for (double treshold : tresh) {
            Performance diabetesUnknownFed = diabetesUnknown(folds, treshold);
            double diabetesUnknown = wekaTest("Outcome",
                                              DIABETES_WEKA_BIF.replace("Missing",
                                                                        "MissingTreshold" + String.valueOf(
                                                                                        treshold)
                                                                                .replace(".", "_"))
                    , TEST_DIABETES_FULL_MISSING.replace("Missing",
                                                         "MissingTreshold" + String.valueOf(treshold)
                                                                 .replace(".", "_")));

            assertEquals(diabetesUnknown, diabetesUnknownFed.getRealAuc(), 0.025);
            assertEquals(diabetesUnknown, diabetesUnknownFed.getSyntheticAuc(), 0.11);
            assertEquals(diabetesUnknown, diabetesUnknownFed.getSyntheticFoldAuc(), 0.025);
        }
    }

    @Test
    public void testVertiBayesKFoldKnown() throws Exception {
        // Federated and non federated variants should have comparable performance
        // Due to the inherent randomness it is possible for the federated setup to outperform the normal setup

        // Full Asia K-fold will take ~6 minutes
        // Full Iris K-fold will take ~33 minutes
        // Full ALARM K-fold will take ~3 hours
        // Full Diabetes K-fold will also take hours
        // Do not start IRIS or ALARM unless you want to wait

        List<Integer> folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
        long start = System.currentTimeMillis();

        Performance asiaFed = asia(folds);
        double asia = wekaTest("lung", ASIA_WEKA_BIF, TEST_ASIA_FULL);

        assertEquals(asia, asiaFed.getRealAuc(), 0.025);
        assertEquals(asia, asiaFed.getSyntheticAuc(), 0.025);
        assertEquals(asia, asiaFed.getSyntheticFoldAuc(), 0.025);


        System.out.println("Asia :" + asia);
        System.out.println("Validating against real data:");
        System.out.println("Federated");
        System.out.println("Asia :" + asiaFed.getRealAuc());

        System.out.println("Validating against full synthetic data:");
        System.out.println("Federated");
        System.out.println("Asia :" + asiaFed.getSyntheticAuc());

        System.out.println("Validating against fold synthetic data:");
        System.out.println("Federated");
        System.out.println("Asia :" + asiaFed.getSyntheticFoldAuc());

        System.out.println("Time: " + (System.currentTimeMillis() - start));
        if (!SMALL_TEST) {
            start = System.currentTimeMillis();

            double diabetesWeka = wekaTest("Outcome", DIABETES_WEKA_BIF, TEST_DIABETES_FULL);
            Performance diabetesFed = diabetes(folds);
            assertEquals(diabetesWeka, diabetesFed.getRealAuc(), 0.025);
            //Using synthetic training data in k-fold results in weird stuff for diabetes, no idea why.
            //Other validation methods have expected results
            assertEquals(diabetesWeka, diabetesFed.getSyntheticAuc(), 0.11);
            assertEquals(diabetesWeka, diabetesFed.getSyntheticFoldAuc(), 0.025);

            System.out.println("diabetes :" + diabetesWeka);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("diabetes :" + diabetesFed.getRealAuc());

            System.out.println("Validating against full synthetic data:");
            System.out.println("Federated");
            System.out.println("diabetes :" + diabetesFed.getSyntheticAuc());

            System.out.println("Validating against fold synthetic data:");
            System.out.println("Federated");
            System.out.println("diabetes :" + diabetesFed.getSyntheticFoldAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();


            Performance irisAutomaticFed = irisAutomatic(folds);
            double irisAutomatic = wekaTest("label", IRIS_WEKA_BIF, TEST_IRIS_FULL);

            assertEquals(irisAutomatic, irisAutomaticFed.getRealAuc(), 0.025);
            assertEquals(irisAutomatic, irisAutomaticFed.getSyntheticAuc(), 0.025);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
            // assertEquals(irisAutomatic, irisAutomaticFed.getSyntheticFoldAuc(), 0.025);

            System.out.println("IrisAutomatic :" + irisAutomatic);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("IrisAutomatic :" + irisAutomaticFed.getRealAuc());

            System.out.println("Validating against full synthetic data:");
            System.out.println("Federated");
            System.out.println("IrisAutomatic :" + irisAutomaticFed.getSyntheticAuc());

            System.out.println("Validating against fold synthetic data:");
            System.out.println("Federated");
            System.out.println("IrisAutomatic :" + irisAutomaticFed.getSyntheticFoldAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();


            Performance irisManualFed = irisManual(folds);
            double irisManual = wekaTest("label", IRIS_WEKA_BIF, TEST_IRIS_FULL);

            assertEquals(irisManual, irisManualFed.getRealAuc(), 0.025);
            assertEquals(irisManual, irisManualFed.getSyntheticAuc(), 0.025);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
            // assertEquals(irisManual, irisManualFed.getSyntheticFoldAuc(), 0.025);

            System.out.println("IrisManual :" + irisManual);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("IrisManual :" + irisManualFed.getRealAuc());

            System.out.println("Validating against full synthetic data:");
            System.out.println("Federated");
            System.out.println("IrisManual :" + irisManualFed.getSyntheticAuc());

            System.out.println("Validating against fold synthetic data:");
            System.out.println("Federated");
            System.out.println("IrisManual :" + irisManualFed.getSyntheticFoldAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();

            Performance alarmFed = alarm(folds);
            double alarm = wekaTest("BP", ALARM_WEKA_BIF, TEST_ALARM_FULL);

            assertEquals(alarm, alarmFed.getRealAuc(), 0.025);
            assertEquals(alarm, alarmFed.getSyntheticAuc(), 0.025);
            assertEquals(alarm, alarmFed.getSyntheticFoldAuc(), 0.025);

            System.out.println("Alarm :" + alarm);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("Alarm :" + alarmFed.getRealAuc());

            System.out.println("Validating against full synthetic data:");
            System.out.println("Federated");
            System.out.println("Alarm :" + alarmFed.getSyntheticAuc());

            System.out.println("Validating against fold synthetic data:");
            System.out.println("Federated");
            System.out.println("Alarm :" + alarmFed.getSyntheticFoldAuc());

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
        // Full Diabetes K-fold will also take hours
        // Do not start IRIS or ALARM unless you want to wait

        List<Integer> folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
        int count = 0;
        for (double treshold : TRESHHOLDS) {
            if (SMALL_TEST && count >= 1) {
                //small test only does the first treshold
                continue;
            }
            count++;

            System.out.println("Treshold: " + treshold);
            long start = System.currentTimeMillis();

            Performance asiaUnknownFed = asiaUnknown(folds, treshold);
            double asiaUnknown = wekaTest("lung",
                                          ASIA_WEKA_BIF.replace("Missing",
                                                                "Treshold" + String.valueOf(treshold)
                                                                        .replace(".", "_")),
                                          TEST_ASIA_FULL_MISSING.replace("Missing",
                                                                         "MissingTreshold" + String.valueOf(treshold)
                                                                                 .replace(".", "_")));

            assertEquals(asiaUnknown, asiaUnknownFed.getRealAuc(), 0.025);
            assertEquals(asiaUnknown, asiaUnknownFed.getSyntheticAuc(), 0.025);
            assertEquals(asiaUnknown, asiaUnknownFed.getSyntheticFoldAuc(), 0.025);

            System.out.println("Asia unknown :" + asiaUnknown);
            System.out.println("Validating against real data:");
            System.out.println("Federated");
            System.out.println("Asia unknown :" + asiaUnknownFed.getRealAuc());

            System.out.println("Validating against full synthetic data:");
            System.out.println("Federated");
            System.out.println("Asia unknown :" + asiaUnknownFed.getSyntheticAuc());

            System.out.println("Validating against fold synthetic data:");
            System.out.println("Federated");
            System.out.println("Asia :" + asiaUnknownFed.getSyntheticFoldAuc());

            System.out.println("Time: " + (System.currentTimeMillis() - start));

            if (!SMALL_TEST) {
                start = System.currentTimeMillis();

                Performance diabetesUnknownFed = diabetesUnknown(folds, treshold);
                double diabetesUnknown = wekaTest("Outcome",
                                                  DIABETES_WEKA_BIF.replace("Missing",
                                                                            "MissingTreshold" + String.valueOf(
                                                                                            treshold)
                                                                                    .replace(".", "_"))
                        , TEST_DIABETES_FULL_MISSING.replace("Missing",
                                                             "MissingTreshold" + String.valueOf(treshold)
                                                                     .replace(".", "_")));

                assertEquals(diabetesUnknown, diabetesUnknownFed.getRealAuc(), 0.025);
                //Using synthetic training data in k-fold results in weird stuff for diabetes, no idea why.
                //Other validation methods have expected results
                assertEquals(diabetesUnknown, diabetesUnknownFed.getSyntheticAuc(), 0.11);
                assertEquals(diabetesUnknown, diabetesUnknownFed.getSyntheticFoldAuc(), 0.025);

                System.out.println("Diabetes unknown :" + diabetesUnknown);
                System.out.println("Validating against real data:");
                System.out.println("Federated");
                System.out.println("Diabetes unknown :" + diabetesUnknownFed.getRealAuc());

                System.out.println("Validating against full synthetic data:");
                System.out.println("Federated");
                System.out.println("Diabetes unknown :" + diabetesUnknownFed.getSyntheticAuc());

                System.out.println("Validating against fold synthetic data:");
                System.out.println("Federated");
                System.out.println("Diabetes :" + diabetesUnknownFed.getSyntheticFoldAuc());

                System.out.println("Time: " + (System.currentTimeMillis() - start));

                start = System.currentTimeMillis();

                Performance irisAutomaticUnknownFed = irisUnknown(folds, treshold, true);
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
                // Synthetic fold AUC for iris is all over the place due to the small folds
                // So ignore it
                // assertEquals(irisAutomaticUnknown, irisAutomaticUnknownFed.getSyntheticFoldAuc(), 0.025);

                System.out.println("IrisAutomatic unknown :" + irisAutomaticUnknown);
                System.out.println("Validating against real data:");
                System.out.println("Federated");
                System.out.println("IrisAutomatic unknown :" + irisAutomaticUnknownFed.getRealAuc());

                System.out.println("Validating against full synthetic data:");
                System.out.println("Federated");
                System.out.println("IrisAutomatic unknown :" + irisAutomaticUnknownFed.getSyntheticAuc());

                System.out.println("Validating against fold synthetic data:");
                System.out.println("Federated");
                System.out.println("IrisAutomatic :" + irisAutomaticUnknownFed.getSyntheticFoldAuc());

                System.out.println("Time: " + (System.currentTimeMillis() - start));

                start = System.currentTimeMillis();

                Performance irisManualUnknownFed = irisUnknown(folds, treshold, false);
                double irisManualUnknown = wekaTest("label",
                                                    IRIS_WEKA_BIF.replace("Missing",
                                                                          "MissingTreshold" + String.valueOf(treshold)
                                                                                  .replace(".", "_")),
                                                    TEST_IRIS_FULL_MISSING.replace(
                                                            "Missing", "MissingTreshold" + String.valueOf(treshold)
                                                                    .replace(".", "_")));

                assertEquals(irisManualUnknown, irisManualUnknownFed.getRealAuc(), 0.05);
                assertEquals(irisManualUnknown, irisManualUnknownFed.getSyntheticAuc(), 0.05);
                // Synthetic fold AUC for iris is all over the place due to the small folds
                // So ignore it
                // assertEquals(irisManualUnknown, irisManualUnknownFed.getSyntheticFoldAuc(), 0.025);

                System.out.println("IrisManual unknown :" + irisManualUnknown);
                System.out.println("Validating against real data:");
                System.out.println("Federated");
                System.out.println("IrisManual unknown :" + irisManualUnknownFed.getRealAuc());

                System.out.println("Validating against full synthetic data:");
                System.out.println("Federated");
                System.out.println("IrisManual unknown :" + irisManualUnknownFed.getSyntheticAuc());

                System.out.println("Validating against fold synthetic data:");
                System.out.println("Federated");
                System.out.println("IrisManual :" + irisManualUnknownFed.getSyntheticFoldAuc());

                System.out.println("Time: " + (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();

                Performance alarmUnknownFed = alarmUnknown(folds, treshold);
                double alarmUnknown = wekaTest("BP",
                                               ALARM_WEKA_BIF.replace("Missing",
                                                                      "MissingTreshold" + String.valueOf(treshold)
                                                                              .replace(".", "_")),
                                               TEST_ALARM_FULL_MISSING.replace(
                                                       "Missing", "MissingTreshold" + String.valueOf(treshold)
                                                               .replace(".", "_")));

                assertEquals(alarmUnknown, alarmUnknownFed.getRealAuc(), 0.05);
                assertEquals(alarmUnknown, alarmUnknownFed.getSyntheticAuc(), 0.05);
                assertEquals(alarmUnknown, alarmUnknownFed.getSyntheticFoldAuc(), 0.05);

                System.out.println("Alarm unknown :" + alarmUnknown);
                System.out.println("Validating against real data:");
                System.out.println("Federated");
                System.out.println("Alarm unknown :" + alarmUnknownFed.getRealAuc());

                System.out.println("Validating against full synthetic data:");
                System.out.println("Federated");
                System.out.println("Alarm unknown :" + alarmUnknownFed.getSyntheticAuc());

                System.out.println("Validating against fold synthetic data:");
                System.out.println("Federated");
                System.out.println("Alarm :" + alarmUnknownFed.getSyntheticFoldAuc());

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
            testVertiBayesFullDataSetDiabetes();
            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            testVertiBayesFullDataSetAlarm();
            System.out.println("Time: " + (System.currentTimeMillis() - start));
        }
        int count = 0;
        for (double d : TRESHHOLDS) {
            start = System.currentTimeMillis();
            testVertiBayesFullDataSetMissingAsia(d);
            System.out.println("Time: " + (System.currentTimeMillis() - start));
            count++;
            if (SMALL_TEST && count > 1) {
                //only do the first treshhold for small test.
                break;
            }
            if (!SMALL_TEST) {
                // do not turn on unless you have the time to wait
                start = System.currentTimeMillis();
                testVertiBayesFullDataSetMissingDiabetes(d);
                System.out.println("Time: " + (System.currentTimeMillis() - start));

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
        double aucManual = buildAndValidate(FIRSTHALF_IRIS, SECONDHALF_IRIS,
                                            readData("label", TEST_IRIS_FULL), "label",
                                            TEST_IRIS_FULL.replace("Weka.arff",
                                                                   ".csv"), buildIrisNetworkBinned()).getRealAuc();

        double aucAutomatic = buildAndValidate(FIRSTHALF_IRIS, SECONDHALF_IRIS,
                                               readData("label", TEST_IRIS_FULL), "label",
                                               TEST_IRIS_FULL.replace("Weka.arff",
                                                                      ".csv"), buildIrisNetworkNoBins()).getRealAuc();

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

        double auc = buildAndValidate(first, second,
                                      readData("label", full), "label",
                                      full.replace(".arff",
                                                   ".csv"), buildIrisNetworkBinnedMissing()).getRealAuc();

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
        double auc = buildAndValidate(FIRSTHALF_ALARM, SECONDHALF_ALARM, readData("BP", TEST_ALARM_FULL),
                                      "BP", TEST_ALARM_FULL.replace("WEKA.arff",
                                                                    ".csv"), buildAlarmNetwork()).getRealAuc();

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
        double auc = buildAndValidate(first, second,
                                      readData("BP", full), "BP", full.replace("WEKA.arff",
                                                                               ".csv"),
                                      buildAlarmNetwork()).getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.88, 0.04);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.88, 0.04);
        }
    }

    private void testVertiBayesFullDataSetDiabetes() throws Exception {
        double auc = buildAndValidate(FIRSTHALF_DIABETES, SECONDHALF_DIABETES,
                                      readData("Outcome", TEST_DIABETES_FULL),
                                      "Outcome", TEST_DIABETES_FULL.replace("WEKA.arff", ".csv"),
                                      buildDiabetesNetwork()).getRealAuc();


        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.85, 0.025);
    }


    private void testVertiBayesFullDataSetAsia() throws Exception {
        double auc = buildAndValidate(FIRSTHALF_ASIA, SECONDHALF_ASIA, readData("lung", TEST_ASIA_FULL),
                                      "lung", TEST_ASIA_FULL.replace("WEKA.arff", ".csv"),
                                      buildAsiaNetwork()).getRealAuc();


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
        double auc = buildAndValidate(first, second, readData("lung", full), "lung",
                                      full.replace(".arff", ".csv"), buildAsiaNetwork()).getRealAuc();

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        if (treshold == 0.05) {
            assertEquals(auc, 0.78, 0.025);
        } else if (treshold == 0.1) {
            assertEquals(auc, 0.70, 0.025);
        }
    }

    private void testVertiBayesFullDataSetMissingDiabetes(double treshold) throws Exception {
        String first = FIRSTHALF_DIABETES_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String second = SECONDHALF_DIABETES_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        String full = TEST_DIABETES_FULL_MISSING.replace("Missing", "MissingTreshold" + String.valueOf(treshold)
                .replace(".", "_"));
        double auc = buildAndValidate(first, second, readData("Outcome", full), "Outcome",
                                      full.replace(".arff", ".csv"), buildDiabetesNetwork()).getRealAuc();

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

    private Performance diabetes(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_Diabetes + ids + ".csv";
            String right = FOLD_RIGHTHALF_Diabetes + ids + ".csv";
            String testFoldarrf = TEST_FOLD_Diabetes + fold + "WEKA.arff";
            String testFoldcsv = TEST_FOLD_Diabetes + fold + ".csv";
            Performance res = buildAndValidate(left, right,
                                               readData("Outcome", testFoldarrf),
                                               "Outcome", testFoldcsv, buildDiabetesNetwork());
            //Quite a lot of variance between folds
            assertEquals(res.getRealAuc(), 0.79, 0.2);
            assertEquals(res.getSyntheticAuc(), 0.79, 0.2);
            assertEquals(res.getSyntheticFoldAuc(), 0.79, 0.2);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        assertEquals(averageAUC, 0.79, 0.025);
        assertEquals(averageAUCSynthetic, 0.99, 0.1);
        assertEquals(averageAUCFoldSynthetic, 0.79, 0.2);
        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }

    private Performance asia(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ASIA + ids + ".csv";
            String right = FOLD_RIGHTHALF_ASIA + ids + ".csv";
            String testFoldarrf = TEST_FOLD_ASIA + fold + "WEKA.arff";
            String testFoldcsv = TEST_FOLD_ASIA + fold + ".csv";
            Performance res = buildAndValidate(left, right,
                                               readData("lung", testFoldarrf),
                                               "lung", testFoldcsv, buildAsiaNetwork());
            assertEquals(res.getRealAuc(), 0.96, 0.05);
            assertEquals(res.getSyntheticAuc(), 0.96, 0.05);
            assertEquals(res.getSyntheticFoldAuc(), 0.96, 0.05);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();

        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        assertEquals(averageAUC, 0.99, 0.025);
        assertEquals(averageAUCSynthetic, 0.99, 0.025);
        assertEquals(averageAUCFoldSynthetic, 0.99, 0.025);
        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }

    private Performance asiaUnknown(List<Integer> folds, double treshold) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
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
            String testFoldarrf = TEST_FOLD_ASIA +
                    "Treshold" + String.valueOf(treshold)
                    .replace(".", "_") + "missing" + fold + "WEKA.arff";
            String testFoldcsv = testFoldarrf.replace("WEKA.arff", ".csv");
            Performance res = buildAndValidate(left, right,
                                               readData("lung", testFoldarrf),
                                               "lung", testFoldcsv, buildAsiaNetwork());
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //The average is still going to be quite close to .78 though
            if (treshold == 0.05) {
                assertEquals(res.getRealAuc(), 0.78, 0.07);
                assertEquals(res.getSyntheticAuc(), 0.78, 0.07);
                assertEquals(res.getSyntheticFoldAuc(), 0.78, 0.08);
            } else if (treshold == 0.1) {
                assertEquals(res.getRealAuc(), 0.70, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.70, 0.1);
                assertEquals(res.getSyntheticFoldAuc(), 0.70, 0.1);
            }
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        if (treshold == 0.05) {
            assertEquals(averageAUC, 0.78, 0.04);
            assertEquals(averageAUCSynthetic, 0.78, 0.04);
            assertEquals(averageAUCFoldSynthetic, 0.78, 0.04);
        } else if (treshold == 0.1) {
            assertEquals(averageAUC, 0.7, 0.04);
            assertEquals(averageAUCSynthetic, 0.7, 0.04);
            assertEquals(averageAUCFoldSynthetic, 0.7, 0.04);
        }
        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }


    private Performance alarm(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ALARM + ids + ".csv";
            String right = FOLD_RIGHTHALF_ALARM + ids + ".csv";
            String testFoldarff = TEST_FOLD_ALARM + fold + "WEKA.arff";
            String testFoldCsv = testFoldarff.replace("WEKA.arff", ".csv");
            Performance res = buildAndValidate(left, right,
                                               readData("BP", testFoldarff),
                                               "BP", testFoldCsv, buildAlarmNetwork());
            assertEquals(res.getRealAuc(), 0.91, 0.025);
            assertEquals(res.getSyntheticAuc(), 0.91, 0.025);
            assertEquals(res.getSyntheticFoldAuc(), 0.91, 0.025);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        assertEquals(averageAUC, 0.91, 0.025);
        assertEquals(averageAUCSynthetic, 0.91, 0.025);
        assertEquals(averageAUCFoldSynthetic, 0.91, 0.025);
        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }

    private Performance diabetesUnknown(List<Integer> folds, double treshold) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
        //unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_Diabetes_MISSING.replace("missing",
                                                                 "Treshold" + String.valueOf(treshold)
                                                                         .replace(".", "_") + "missing") + ids + ".csv";
            String right = FOLD_RIGHTHALF_Diabetes_MISSING.replace("missing",
                                                                   "Treshold" + String.valueOf(treshold)
                                                                           .replace(".",
                                                                                    "_") + "missing") + ids + ".csv";

            String testFoldarff = TEST_FOLD_Diabetes + "Treshold" + String.valueOf(treshold)
                    .replace(".",
                             "_") + "missing" + fold + "WEKA.arff";
            String testFoldCsv = testFoldarff.replace("WEKA.arff", ".csv");
            Performance res = buildAndValidate(left, right,
                                               readData("Outcome", testFoldarff),
                                               "Outcome", testFoldCsv, buildDiabetesNetwork());
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            if (treshold == 0.05) {
                assertEquals(res.getRealAuc(), 0.76, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.76, 0.15);
                assertEquals(res.getSyntheticFoldAuc(), 0.76, 0.11);
            } else if (treshold == 0.1) {
                assertEquals(res.getRealAuc(), 0.74, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.74, 0.15);
                assertEquals(res.getSyntheticFoldAuc(), 0.74, 0.11);
            } else if (treshold == 0.3) {
                assertEquals(res.getRealAuc(), 0.73, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.73, 0.15);
                assertEquals(res.getSyntheticFoldAuc(), 0.73, 0.11);
            }
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        if (treshold == 0.05) {
            assertEquals(averageAUC, 0.78, 0.05);
            assertEquals(averageAUCSynthetic, 0.88, 0.05);
            assertEquals(averageAUCFoldSynthetic, 0.78, 0.05);
        } else if (treshold == 0.1) {
            assertEquals(averageAUC, 0.74, 0.05);
            assertEquals(averageAUCSynthetic, 0.84, 0.05);
            assertEquals(averageAUCFoldSynthetic, 0.74, 0.05);
        } else if (treshold == 0.3) {
            assertEquals(averageAUC, 0.73, 0.05);
            assertEquals(averageAUCSynthetic, 0.83, 0.05);
            assertEquals(averageAUCFoldSynthetic, 0.73, 0.05);
        }

        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }

    private Performance alarmUnknown(List<Integer> folds, double treshold) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
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

            String testFoldarff = TEST_FOLD_ALARM + "Treshold" + String.valueOf(treshold)
                    .replace(".",
                             "_") + "missing" + fold + "WEKA.arff";
            String testFoldCsv = testFoldarff.replace("WEKA.arff", ".csv");
            Performance res = buildAndValidate(left, right,
                                               readData("BP", testFoldarff),
                                               "BP", testFoldCsv, buildAlarmNetwork());
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //The average is still going to be quite close to .88 though
            if (treshold == 0.05) {
                assertEquals(res.getRealAuc(), 0.86, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.86, 0.1);
                assertEquals(res.getSyntheticFoldAuc(), 0.86, 0.1);
            } else if (treshold == 0.1) {
                assertEquals(res.getRealAuc(), 0.80, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.80, 0.1);
                assertEquals(res.getSyntheticFoldAuc(), 0.80, 0.1);
            }
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        if (treshold == 0.05) {
            assertEquals(averageAUC, 0.88, 0.05);
            assertEquals(averageAUCSynthetic, 0.88, 0.05);
            assertEquals(averageAUCFoldSynthetic, 0.88, 0.05);
        } else if (treshold == 0.1) {
            assertEquals(averageAUC, 0.80, 0.1);
            assertEquals(averageAUCSynthetic, 0.80, 0.1);
            assertEquals(averageAUCFoldSynthetic, 0.80, 0.1);
        }

        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }

    private Performance irisAutomatic(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_IRIS + ids + ".csv";
            String right = FOLD_RIGHTHALF_IRIS + ids + ".csv";
            String testFoldarff = TEST_FOLD_IRIS + fold + "WEKA.arff";
            String testFoldCsv = testFoldarff.replace("WEKA.arff", ".csv");
            Performance res = buildAndValidate(left, right,
                                               readData("label", testFoldarff),
                                               "label", testFoldCsv, buildIrisNetworkNoBins());
            assertEquals(res.getRealAuc(), 0.96, 0.05);
            assertEquals(res.getSyntheticAuc(), 0.96, 0.05);
            // Synthetic fold AUC is all over the place due to the small folds
            // So ignore it
            //assertEquals(res.getSyntheticFoldAuc(), 0.96, 0.05);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        assertEquals(averageAUC, 0.96, 0.05);
        assertEquals(averageAUCSynthetic, 0.96, 0.05);
        // Synthetic fold AUC for iris is all over the place due to the small folds
        // So ignore it
        //  assertEquals(averageAUCFoldSynthetic, 0.96, 0.05);
        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }

    private Performance irisManual(List<Integer> folds) throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_IRIS + ids + ".csv";
            String right = FOLD_RIGHTHALF_IRIS + ids + ".csv";

            String testFoldarff = TEST_FOLD_IRIS + fold + "WEKA.arff";
            String testFoldCsv = testFoldarff.replace("WEKA.arff", ".csv");
            Performance res = buildAndValidate(left, right,
                                               readData("label", testFoldarff),
                                               "label", testFoldCsv, buildIrisNetworkBinned());
            assertEquals(res.getRealAuc(), 0.90, 0.1);
            assertEquals(res.getSyntheticAuc(), 0.90, 0.1);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
            // assertEquals(res.getSyntheticFoldAuc(), 0.96, 0.05);
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        assertEquals(averageAUC, 0.90, 0.1);
        assertEquals(averageAUCSynthetic, 0.90, 0.1);
        // Synthetic fold AUC for iris is all over the place due to the small folds
        // So ignore it
        // assertEquals(averageAUCFoldSynthetic, 0.96, 0.05);
        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }

    private Performance irisUnknown(List<Integer> folds, double treshold, boolean automaticBinning)
            throws Exception {
        double aucSum = 0;
        double aucSumSynthetic = 0;
        double aucSumFoldSynthetic = 0;
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
            Performance res = null;

            String testFoldarff = TEST_FOLD_IRIS + "Treshold" + String.valueOf(
                    treshold).replace(".", "_") +
                    "missing" + fold + "WEKA.arff";
            String testFoldCsv = testFoldarff.replace("WEKA.arff", ".csv");
            if (automaticBinning) {
                res = buildAndValidate(left, right,
                                       readData("label", testFoldarff),
                                       "label", testFoldCsv, buildIrisNetworkNoBins());
            } else {
                res = buildAndValidate(left, right,
                                       readData("label", testFoldarff),
                                       "label", testFoldCsv, buildIrisNetworkBinnedMissing());
            }
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //Hence the wide range
            //The average is still going to be quite close to .96 though
            if (treshold == 0.05) {
                assertEquals(res.getRealAuc(), 0.90, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.90, 0.1);
                // Synthetic fold AUC for iris is all over the place due to the small folds
                // So ignore it
                // assertEquals(res.getSyntheticFoldAuc(), 0.90, 0.1);
            } else if (treshold == 0.1) {
                assertEquals(res.getRealAuc(), 0.90, 0.1);
                assertEquals(res.getSyntheticAuc(), 0.90, 0.1);
                // Synthetic fold AUC for iris is all over the place due to the small folds
                // So ignore it
                // assertEquals(res.getSyntheticFoldAuc(), 0.90, 0.1);
            }
            aucSum += res.getRealAuc();
            aucSumSynthetic += res.getSyntheticAuc();
            aucSumFoldSynthetic += res.getSyntheticFoldAuc();
        }
        double averageAUC = aucSum / folds.size();
        double averageAUCSynthetic = aucSumSynthetic / folds.size();
        double averageAUCFoldSynthetic = aucSumFoldSynthetic / folds.size();
        if (treshold == 0.05) {
            assertEquals(averageAUC, 0.96, 0.04);
            assertEquals(averageAUCSynthetic, 0.96, 0.04);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
            // assertEquals(averageAUCFoldSynthetic, 0.96, 0.04);
        } else if (treshold == 0.1) {
            assertEquals(averageAUC, 0.96, 0.04);
            assertEquals(averageAUCSynthetic, 0.96, 0.04);
            // Synthetic fold AUC for iris is all over the place due to the small folds
            // So ignore it
            // assertEquals(averageAUCFoldSynthetic, 0.96, 0.04);
        }

        Performance tuple = new Performance();
        tuple.setRealAuc(averageAUC);
        tuple.setSyntheticAuc(averageAUCSynthetic);
        tuple.setSyntheticFoldAuc(averageAUCFoldSynthetic);
        return tuple;
    }

}
