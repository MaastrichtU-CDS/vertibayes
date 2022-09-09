package com.florian.vertibayes.weka.performance;

import com.florian.vertibayes.weka.performance.tests.*;
import com.florian.vertibayes.weka.performance.tests.util.Performance;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestPerformance {
    // IMPORTANT TO NOTE; IF THESE TEST BEHAVE WEIRDLY MANUALLY CHECK IN WEKA.
    // ISSUES LIKE MISALIGNED COLLUMNS LEAD TO WEIRD RESULTS WEKA WILL SHOW THIS BY THROWING AN ERROR
    // TO TEST MANUALLY USE TEST.ARFF THAT IS GENERATED DURING THESE TEST CASES AS THE BASELINE THEN COMPARE TO
    // THE CORRESPONDING TEST FILE

    // DIABETES TAKES ABOUT 27 seconds TO TRAIN ONCE (few bins variant)
    // ALARM TAKES ABOUT 265 seconds
    // IRIS ABOUT 12 seconds (automatic binning)
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
    private static final List<Double> TRESHHOLDS = Arrays.asList(0.05, 0.1, 0.3);
    private static final boolean SMALL_TEST = false;

    @Test
    public void smallTest() throws Exception {
        //Small test to check everything still works
        if (SMALL_TEST) {
            long start = System.currentTimeMillis();
            Performance asia = Asia.kFold();
            printResults(start, asia, 0);

            start = System.currentTimeMillis();
            Performance diabetes = DiabetesFewBins.kFold();
            printResults(start, diabetes, 0);

            System.out.println("Treshold: " + 0.05);

            start = System.currentTimeMillis();
            Performance asiaUnknown = Asia.kFoldUnknown(0.05);
            printResults(start, asiaUnknown, 0.05);

            start = System.currentTimeMillis();
            Performance diabetesUnknown = DiabetesFewBins.kFoldUnknown(0.05);
            printResults(start, diabetesUnknown, 0.05);

            start = System.currentTimeMillis();
            Asia.testVertiBayesFullDataSet();
            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            Asia.testVertiBayesFullDataSetMissing(0.05);
            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            DiabetesFewBins.testVertiBayesFullDataSet();
            System.out.println("Time: " + (System.currentTimeMillis() - start));

            start = System.currentTimeMillis();
            DiabetesFewBins.testVertiBayesFullDataSetMissing(0.05);
            System.out.println("Time: " + (System.currentTimeMillis() - start));
        }
    }

    @Test
    public void runtTimeTest() throws Exception {
        if (!SMALL_TEST) {
            int count = 25;

            long smallSet = 0;
            long baseSet = 0;
            long tenValueSet = 0;
            long twoParentSet = 0;
            long fourValueSet = 0;

            for (int i = 0; i < count; i++) {
                long start = System.currentTimeMillis();
                RuntimeTest.testSmallDataset();
                smallSet += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                RuntimeTest.testRegularDatasetMultipleParents();
                twoParentSet += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                RuntimeTest.testRegularDataset();
                baseSet += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                RuntimeTest.testRegularDatasetTenValues();
                tenValueSet += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                RuntimeTest.testRegularDatasetFourValues();
                fourValueSet += System.currentTimeMillis() - start;
            }

            System.out.println("BaseSet: " + baseSet / count);
            System.out.println(
                    "Baseset contains: 5032 individuals, 3 nodes x1 - x2 - x3, x1.parents = {} x2.parents = {x1}, x3" +
                            ".parents={x2}, x1, x2 & x3 all have 2 values {0,1}.\n This results in 10 probabilities " +
                            "that " +
                            "needed to be calculated.");
            System.out.println("Smallset: " + smallSet / count);
            System.out.println("Smallset contains 10 individuals, the rest is the same as baseset");
            System.out.println("Multipleparents: " + twoParentSet / count);
            System.out.println(
                    "Multipleparents gives x3.parents={x2, x3}, resulting in 14 probabilities that need to be " +
                            "calculated " +
                            "the rest is the same as baseset");
            System.out.println("fourValueSet: " + fourValueSet / count);
            System.out.println(
                    "fourValueSet gives x1{0,1,2,3}, resulting in 14 probabilities that need to be calculated " +
                            "the rest is the same as baseset");
            System.out.println("tenValueSet: " + tenValueSet / count);
            System.out.println(
                    "tenValueSet gives x1{0,1,2,3,4,5,6,7,8,9}, resulting in 34 probabilities that need to be " +
                            "calculated " +
                            "the rest is the same as baseset");
        }
    }

    @Test
    public void testVertiBayesKFoldKnown() throws Exception {
        if (!SMALL_TEST) {
            long start = System.currentTimeMillis();
            Performance asia = Asia.kFold();
            printResults(start, asia, 0);


            start = System.currentTimeMillis();
            Performance irisAutomatic = IrisAutomatic.kFold();
            printResults(start, irisAutomatic, 0);

            start = System.currentTimeMillis();
            Performance irisManual = IrisManual.kFold();
            printResults(start, irisManual, 0);

            start = System.currentTimeMillis();
            Performance diabetesFewestBins = DiabetesFewestBins.kFold();
            printResults(start, diabetesFewestBins, 0);

            start = System.currentTimeMillis();
            Performance diabetesFewBins = DiabetesFewBins.kFold();
            printResults(start, diabetesFewBins, 0);

            start = System.currentTimeMillis();
            Performance diabetes = Diabetes.kFold();
            printResults(start, diabetes, 0);

            start = System.currentTimeMillis();
            Performance alarm = Alarm.kFold();
            printResults(start, alarm, 0);
        }
    }

    @Test
    public void testVertiBayesKFoldUnKnown() throws Exception {
        if (!SMALL_TEST) {
            int count = 0;
            for (double treshold : TRESHHOLDS) {
                count++;
                System.out.println("Treshold: " + treshold);

                long start = System.currentTimeMillis();
                Performance asiaUnknown = Asia.kFoldUnknown(treshold);
                printResults(start, asiaUnknown, treshold);

                start = System.currentTimeMillis();
                Performance irisAutomaticUnknown = IrisAutomatic.kFoldUnknown(treshold);
                printResults(start, irisAutomaticUnknown, treshold);

                start = System.currentTimeMillis();
                Performance irisManualUnknown = IrisManual.kFoldUnknown(treshold);
                printResults(start, irisManualUnknown, treshold);

                start = System.currentTimeMillis();
                Performance diabetesFewestBins = DiabetesFewestBins.kFoldUnknown(treshold);
                printResults(start, diabetesFewestBins, treshold);

                start = System.currentTimeMillis();
                Performance diabetesFewBins = DiabetesFewBins.kFoldUnknown(treshold);
                printResults(start, diabetesFewBins, treshold);

                start = System.currentTimeMillis();
                Performance diabetesUnknown = Diabetes.kFoldUnknown(treshold);
                printResults(start, diabetesUnknown, treshold);

                start = System.currentTimeMillis();
                Performance alarmUnknown = Alarm.kFoldUnknown(treshold);
                printResults(start, alarmUnknown, treshold);
            }
        }
    }

    @Test
    public void testVertiBayesNoFold() throws Exception {
        if (!SMALL_TEST) {
            long start = System.currentTimeMillis();
            Performance p = Asia.testVertiBayesFullDataSet();
            p.setName("Asia");
            printResults(start, p, 0.0);

            start = System.currentTimeMillis();
            p = IrisManual.testVertiBayesFullDataSet();
            p.setName("Irismanual");
            printResults(start, p, 0.0);

            start = System.currentTimeMillis();
            p = IrisAutomatic.testVertiBayesFullDataSet();
            p.setName("IrisAutomatic");
            printResults(start, p, 0.0);

            start = System.currentTimeMillis();
            p = DiabetesFewestBins.testVertiBayesFullDataSet();
            p.setName("Diabetes fewest bins");
            printResults(start, p, 0.0);


            start = System.currentTimeMillis();
            p = DiabetesFewBins.testVertiBayesFullDataSet();
            p.setName("Diabetes few bins");
            printResults(start, p, 0.0);

            start = System.currentTimeMillis();
            p = Diabetes.testVertiBayesFullDataSet();
            p.setName("Diabetes");
            printResults(start, p, 0.0);

            start = System.currentTimeMillis();
            p = Alarm.testVertiBayesFullDataSet();
            p.setName("Alarm");
            printResults(start, p, 0.0);

            for (double d : TRESHHOLDS) {
                start = System.currentTimeMillis();
                p = Asia.testVertiBayesFullDataSetMissing(d);
                p.setName("Asia");
                printResults(start, p, d);

                start = System.currentTimeMillis();
                p = IrisManual.testVertiBayesFullDataSetMissing(d);
                p.setName("IrisManual");
                printResults(start, p, d);

                start = System.currentTimeMillis();
                p = IrisAutomatic.testVertiBayesFullDataSetMissing(d);
                p.setName("IrisAutomatic");
                printResults(start, p, d);

                start = System.currentTimeMillis();
                p = DiabetesFewestBins.testVertiBayesFullDataSetMissing(d);
                p.setName("Diabetes fewest bins");
                printResults(start, p, d);

                start = System.currentTimeMillis();
                p = DiabetesFewBins.testVertiBayesFullDataSetMissing(d);
                p.setName("Diabetes few bins");
                printResults(start, p, d);

                start = System.currentTimeMillis();
                p = Diabetes.testVertiBayesFullDataSetMissing(d);
                p.setName("Diabetes");
                printResults(start, p, d);

                start = System.currentTimeMillis();
                p = Alarm.testVertiBayesFullDataSetMissing(d);
                p.setName("Alarm");
                printResults(start, p, d);
            }
        }
    }

    public static void printResults(long start, Performance performance, double treshold) {
        System.out.println("Dataset: " + performance.getName() + " Unknown level: " + treshold);
        System.out.println("Central performance: " + performance.getWekaAuc());
        System.out.println("Validating against real data:");
        System.out.println(performance.getRealAuc());

        System.out.println("Validating against full synthetic data:");
        System.out.println(performance.getSyntheticAuc());

        System.out.println("Validating against fold synthetic data:");
        System.out.println(performance.getSyntheticFoldAuc());

        System.out.println("AIC score real data:");
        System.out.println(performance.getAIC());

        System.out.println("AIC score weka:");
        System.out.println(performance.getWekaAIC());

        System.out.println("Errors only present on Federated side: " + performance.getUniqueErrors()[0]);
        System.out.println("Errors only present on Weka side: " + performance.getUniqueErrors()[1]);
        System.out.println("Errors present on both sides: " + performance.getUniqueErrors()[2]);

        System.out.println("Time taken in ms: " + (System.currentTimeMillis() - start));
    }
}
