package com.florian.vertibayes.weka.performance;

import com.florian.vertibayes.weka.performance.tests.Alarm;
import com.florian.vertibayes.weka.performance.tests.Asia;
import com.florian.vertibayes.weka.performance.tests.Diabetes;
import com.florian.vertibayes.weka.performance.tests.Iris;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestPerformance {
    // IMPORTANT TO NOTE; IF THESE TEST BEHAVE WEIRDLY MANUALLY CHECK IN WEKA.
    // ISSUES LIKE MISALIGNED COLLUMNS LEAD TO WEIRD RESULTS WEKA WILL SHOW THIS BY THROWING AN ERROR
    // TO TEST MANUALLY USE TEST.ARFF THAT IS GENERATED DURING THESE TEST CASES AS THE BASELINE THEN COMPARE TO
    // THE CORRESPONDING TEST FILE

    // DIABETES TAKES ABOUT 6 MINUTES TO TRAIN ONCE
    // ALARM TAKES ABOUT 2.5 MINUTES TO TRAIN ONCE
    // IRIS ABOUT 0.75
    // ASIA ABOUT 10 seconds
    // THIS IS ASSUMING NO SYNTHETIC FOLD. THIS IS ALSO ONLY 1 FOLD IN THE CASE OF K-FOLD.
    // MISSING DATA ARE EVEN SLOWER DUE TO THE EXTRA CPD'S THAT NEED TO BE CALCULATED (ALARM GOES TO 11)
    // BOTTLENECK IS POSED BY NUMBER OF VALUES IN CPD WHICH IS DEPENDEND ON THE NUMBER OF UNIQUE ATTRIBUTE VALUES
    // (OR BINS) AS WELL AS THE AMOUNT OF PARENT-CHILD RELATIONSHIPS (E.G. 1 PARENT WITH 2 VALUES FOR A CHILD WITH 2
    // VALUES = 4 VALUES IN THE CPD, 2 PARENTS WITH 2 VALUES = 8 VALUES IN THE CPD)

    // KEEP SMALL_TEST = true FOR BUILDING, OTHERWISE IT'L EASILY TAKE HALF A DAY

    // SKIPPING SYNTHETIC FOLD TESTING WILL CUT THE TIME IN HALF

    // IMPORTANT NOTE 2: DUE TO THE RANDOM NATURE OF EM,DATA GENERATION & THE FOLDS IT IS POSSIBLE TO GET THE
    // OCCASIONAL TERRIBLE PERFORMANCE, ESPECIALLY ON INDIVIDUAL FOLDS. RERUN THE TEST AND SEE IF IT HAPPENS AGAIN.
    private static final List<Double> TRESHHOLDS = Arrays.asList(0.05, 0.1);
    private static final boolean SMALL_TEST = true;

//    public void testVertiBayesDiabetes() throws Exception {
//        List<Double> tresh = new ArrayList<>();
//        tresh.addAll(TRESHHOLDS);
//        tresh.add(0.3);
//        for (double treshold : tresh) {
//            Performance diabetesUnknownFed = DiabetesTest.kFoldUnknown(treshold);
//            double diabetesUnknown = DiabetesTest.weka(treshold);
//
//            assertEquals(diabetesUnknown, diabetesUnknownFed.getRealAuc(), 0.025);
//            assertEquals(diabetesUnknown, diabetesUnknownFed.getSyntheticAuc(), 0.11);
//            assertEquals(diabetesUnknown, diabetesUnknownFed.getSyntheticFoldAuc(), 0.025);
//        }
//    }

    @Test
    public void testVertiBayesKFoldKnown() throws Exception {
        long start = System.currentTimeMillis();
        Performance asia = Asia.kFold();
        printResults(start, asia, "Asia :", 0);

        if (!SMALL_TEST) {
            start = System.currentTimeMillis();
            Performance diabetes = Diabetes.kFold();
            printResults(start, diabetes, "diabetes :", 0);

            start = System.currentTimeMillis();
            Performance irisAutomatic = Iris.kFold(true);
            printResults(start, irisAutomatic, "IrisAutomatic :", 0);

            start = System.currentTimeMillis();
            Performance irisManual = Iris.kFold(false);
            printResults(start, irisManual, "IrisManual :", 0);

            start = System.currentTimeMillis();
            Performance alarm = Alarm.kFold();
            printResults(start, alarm, "Alarm :", 0);
        }
    }

    @Test
    public void testVertiBayesKFoldUnKnown() throws Exception {
        int count = 0;
        for (double treshold : TRESHHOLDS) {
            if (SMALL_TEST && count >= 1) {
                //small test only does the first treshold
                break;
            }
            count++;
            System.out.println("Treshold: " + treshold);

            long start = System.currentTimeMillis();
            Performance asiaUnknown = Asia.kFoldUnknown(treshold);
            printResults(start, asiaUnknown, "Asia :", treshold);

            if (!SMALL_TEST) {
                start = System.currentTimeMillis();
                Performance diabetesUnknown = Diabetes.kFoldUnknown(treshold);
                printResults(start, diabetesUnknown, "Diabetes :", treshold);

                start = System.currentTimeMillis();
                Performance irisAutomaticUnknown = Iris.kFoldUnknown(true, treshold);
                printResults(start, irisAutomaticUnknown, "IrisAutomatic", treshold);

                start = System.currentTimeMillis();
                Performance irisManualUnknown = Iris.kFoldUnknown(false, treshold);
                printResults(start, irisManualUnknown, "IrisManual", treshold);

                start = System.currentTimeMillis();
                Performance alarmUnknown = Alarm.kFoldUnknown(treshold);
                printResults(start, alarmUnknown, "Alarm", treshold);
            }
        }
    }

    @Test
    public void testVertiBayesNoFold() throws Exception {
        long start = System.currentTimeMillis();
        Asia.testVertiBayesFullDataSet();
        System.out.println("Time: " + (System.currentTimeMillis() - start));

        // do not turn on ALARM, IRIS or DIABETES unless you have the time to wait
        if (!SMALL_TEST) {
            start = System.currentTimeMillis();
            Iris.testVertiBayesFullDataSet();
            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            Diabetes.testVertiBayesFullDataSet();
            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            Alarm.testVertiBayesFullDataSet();
            System.out.println("Time: " + (System.currentTimeMillis() - start));
        }
        int count = 0;
        for (double d : TRESHHOLDS) {
            start = System.currentTimeMillis();
            Asia.testVertiBayesFullDataSetMissing(d);
            System.out.println("Time: " + (System.currentTimeMillis() - start));
            count++;
            if (SMALL_TEST && count > 1) {
                //only do the first treshhold for small test.
                break;
            }
            if (!SMALL_TEST) {
                // do not turn on unless you have the time to wait
                start = System.currentTimeMillis();
                Diabetes.testVertiBayesFullDataSetMissing(d);
                System.out.println("Time: " + (System.currentTimeMillis() - start));

                start = System.currentTimeMillis();
                Iris.testVertiBayesFullDataSetMissing(d);
                System.out.println("Time: " + (System.currentTimeMillis() - start));

                start = System.currentTimeMillis();
                Alarm.testVertiBayesFullDataSetMissing(d);
                System.out.println("Time: " + (System.currentTimeMillis() - start));
            }
        }
    }

    private void printResults(long start, Performance performance, String name, double treshold) {
        System.out.println("Dataset: " + name + " Unknown level: " + treshold);
        System.out.println("Central performance: " + performance.getWekaAuc());
        System.out.println("Validating against real data:");
        System.out.println(performance.getRealAuc());

        System.out.println("Validating against full synthetic data:");
        System.out.println(performance.getSyntheticAuc());

        System.out.println("Validating against fold synthetic data:");
        System.out.println(performance.getSyntheticFoldAuc());

        System.out.println("Time taken in ms: " + (System.currentTimeMillis() - start));
    }
}
