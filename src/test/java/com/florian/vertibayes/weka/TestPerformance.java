package com.florian.vertibayes.weka;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationResponse;
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

    // IMPORTANT TO NOTE; IF THESE TEST BEHAVE WEIRDLY MANUALLY CHECK IN WEKA.
    // ISSUES LIKE MISALIGNED COLLUMNS LEAD TO WEIRD RESULTS WEKA WILL SHOW THIS BY THROWING AN ERROR
    // TO TEST MANUALLY USE TEST.ARFF THAT IS GENERATED DURING THESE TEST CASES AS THE BASELINE THEN COMPARE TO
    // THE CORRESPONDING TEST FILE

    // ALARM TAKES ABOUT 11 MINUTES TO TRAIN ONCE
    // IRIS ABOUT 1
    // ASIA ABOUT 5
    // MISSING DATA ARE EVEN SLOWER DUE TO THE EXTRA CPD'S THAT NEED TO BE CALCULATED

    // FULL K-FOLD TESTCASE WILL TAKE HOURS

    @Test
    public void testVertiBayesKFold() throws Exception {
        // Federated and non federated variants should have comparable performance
        // Due to the inherent randomness it is possible for the federated setup to outperform the normal setup
        List<Integer> folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }
        double irisFed = iris(folds);
        double irisUnknownFed = irisUnknown(folds);
        double iris = wekaTest("label", IRIS_WEKA_BIF, TEST_IRIS_FULL);
        double irisUnknown = wekaTest("label", IRIS_WEKA_BIF, TEST_IRIS_FULL_MISSING);

        assertEquals(iris, irisFed, 0.025);
        assertEquals(irisUnknown, irisUnknownFed, 0.025);

        System.out.println("Iris :" + iris);
        System.out.println("Iris unknown :" + irisUnknown);

        System.out.println("Federated");
        System.out.println("Iris :" + irisFed);
        System.out.println("Iris unknown :" + irisUnknownFed);

        // only turn these bottom two sets on for full testing, they take a long time

//        double asiaFed = asia(folds);
//        double asiaUnknownFed = asiaUnknown(folds);
//        double asia = wekaTest("lung", ASIA_WEKA_BIF, TEST_ASIA_FULL);
//        double asiaUnknown = wekaTest("lung", ASIA_WEKA_BIF, TEST_ASIA_FULL_MISSING);
//
//        assertEquals(asia, asiaFed, 0.025);
//        assertEquals(asiaUnknown, asiaUnknownFed, 0.025);
//
//        System.out.println("Asia :" + asia);
//        System.out.println("Asia unknown :" + asiaUnknown);
//
//        System.out.println("Federated");
//        System.out.println("Asia :" + asiaFed);
//        System.out.println("Asia unknown :" + asiaUnknownFed);
//
//        double alarmUnknownFed = alarmUnknown(folds);
//        double alarm = wekaTest("BP", ALARM_WEKA_BIF, TEST_ALARM_FULL);
//        double alarmUnknown = wekaTest("BP", ALARM_WEKA_BIF, TEST_ALARM_FULL_MISSING);
//        double alarmFed = alarm(folds);
//
//        assertEquals(alarm, alarmFed, 0.025);
//        assertEquals(alarmUnknown, alarmUnknownFed, 0.025);
//
//        System.out.println("Alarm :" + alarm);
//        System.out.println("Alarm unknown :" + alarmUnknown);
//
//        System.out.println("Federated");
//        System.out.println("Alarm :" + alarmFed);
//        System.out.println("Alarm unknown :" + alarmUnknownFed);
    }

    @Test
    public void testVertiBayesNoFold() throws Exception {
        testVertiBayesFullDataSetIris();
        testVertiBayesFullDataSetMissingIris();
        //only turn these bottom two sets on for full testing, they take a long time

//        testVertiBayesFullDataSetAsia();
//        testVertiBayesFullDataSetMissingAsia();
//
//        testVertiBayesFullDataSetAlarm();
//        testVertiBayesFullDataSetMissingAlarm();
    }

    private void testVertiBayesFullDataSetIris() throws Exception {
        double auc = vertiBayesIrisTest(FIRSTHALF_IRIS, SECONDHALF_IRIS, readData("label", TEST_IRIS_FULL), "label");

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.98, 0.025);
    }

    private void testVertiBayesFullDataSetMissingIris() throws Exception {
        double auc = vertiBayesIrisMissingTest(FIRSTHALF_IRIS_MISSING, SECONDHALF_IRIS_MISSING,
                                               readData("label", TEST_IRIS_FULL_MISSING), "label");

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.98, 0.025);
    }

    private void testVertiBayesFullDataSetAlarm() throws Exception {
        double auc = vertiBayesAlarmTest(FIRSTHALF_ALARM, SECONDHALF_ALARM, readData("BP", TEST_ALARM_FULL),
                                         "BP");

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.91, 0.025);
    }


    private void testVertiBayesFullDataSetMissingAlarm() throws Exception {
        double auc = vertiBayesAlarmTest(FIRSTHALF_ALARM_MISSING, SECONDHALF_ALARM_MISSING,
                                         readData("BP", TEST_ALARM_FULL_MISSING), "BP");

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.88, 0.025);
    }


    private void testVertiBayesFullDataSetAsia() throws Exception {
        double auc = vertiBayesAsiaTest(FIRSTHALF_ASIA, SECONDHALF_ASIA, readData("lung", TEST_ASIA_FULL),
                                        "lung");

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.98, 0.025);
    }


    private void testVertiBayesFullDataSetMissingAsia() throws Exception {
        double auc = vertiBayesAsiaTest(FIRSTHALF_ASIA_MISSING, SECONDHALF_ASIA_MISSING,
                                        readData("lung", TEST_ASIA_FULL_MISSING), "lung");

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.78, 0.025);
    }

    private double asia(List<Integer> folds) throws Exception {
        List<Double> auc = new ArrayList<>();
        double aucSum = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ASIA + ids + ".csv";
            String right = FOLD_RIGHTHALF_ASIA + ids + ".csv";
            auc.add(vertiBayesAsiaTest(left, right, readData("lung", TEST_FOLD_ASIA + fold + "WEKA.arff"),
                                       "lung"));
            assertEquals(auc.get(auc.size() - 1), 0.96, 0.05);
            aucSum += auc.get(auc.size() - 1);
        }
        double averageAUC = aucSum / folds.size();
        assertEquals(averageAUC, 0.99, 0.025);
        return averageAUC;
    }

    private double asiaUnknown(List<Integer> folds) throws Exception {
        List<Double> auc = new ArrayList<>();
        double aucSum = 0;
        //unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ASIA_MISSING + ids + ".csv";
            String right = FOLD_RIGHTHALF_ASIA_MISSING + ids + ".csv";
            auc.add(vertiBayesAsiaTest(left, right,
                                       readData("lung", TEST_FOLD_ASIA + "missing" + fold + "WEKA.arff"),
                                       "lung"));
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //The average is still going to be quite close to .78 though
            assertEquals(auc.get(auc.size() - 1), 0.78, 0.04);
            aucSum += auc.get(auc.size() - 1);
        }
        double averageAUC = aucSum / folds.size();
        assertEquals(averageAUC, 0.78, 0.04);
        return averageAUC;
    }


    private double alarm(List<Integer> folds) throws Exception {
        List<Double> auc = new ArrayList<>();
        double aucSum = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ALARM + ids + ".csv";
            String right = FOLD_RIGHTHALF_ALARM + ids + ".csv";
            auc.add(vertiBayesAlarmTest(left, right, readData("BP", TEST_FOLD_ALARM + fold + "WEKA.arff"),
                                        "BP"));
            assertEquals(auc.get(auc.size() - 1), 0.91, 0.025);
            aucSum += auc.get(auc.size() - 1);
        }
        double averageAUC = aucSum / folds.size();
        assertEquals(averageAUC, 0.91, 0.025);
        return averageAUC;
    }

    private double alarmUnknown(List<Integer> folds) throws Exception {
        List<Double> auc = new ArrayList<>();
        double aucSum = 0;
        //unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_ALARM_MISSING + ids + ".csv";
            String right = FOLD_RIGHTHALF_ALARM_MISSING + ids + ".csv";
            auc.add(vertiBayesAlarmTest(left, right,
                                        readData("BP", TEST_FOLD_ALARM + "missing" + fold + "WEKA.arff"),
                                        "BP"));
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //The average is still going to be quite close to .88 though
            assertEquals(auc.get(auc.size() - 1), 0.88, 0.025);
            aucSum += auc.get(auc.size() - 1);
        }
        double averageAUC = aucSum / folds.size();
        assertEquals(averageAUC, 0.88, 0.025);
        return averageAUC;
    }

    private double iris(List<Integer> folds) throws Exception {
        List<Double> auc = new ArrayList<>();
        double aucSum = 0;
        //no unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_IRIS + ids + ".csv";
            String right = FOLD_RIGHTHALF_IRIS + ids + ".csv";
            auc.add(vertiBayesIrisTest(left, right, readData("label", TEST_FOLD_IRIS + fold + "WEKA.arff"),
                                       "label"));
            assertEquals(auc.get(auc.size() - 1), 0.96, 0.05);
            aucSum += auc.get(auc.size() - 1);
        }
        double averageAUC = aucSum / folds.size();
        assertEquals(averageAUC, 0.96, 0.05);
        return averageAUC;
    }

    private double irisUnknown(List<Integer> folds) throws Exception {
        List<Double> auc = new ArrayList<>();
        double aucSum = 0;
        //unknowns
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = FOLD_LEFTHALF_IRIS_MISSING + ids + ".csv";
            String right = FOLD_RIGHTHALF_IRIS_MISSING + ids + ".csv";
            auc.add(vertiBayesIrisMissingTest(left, right,
                                              readData("label", TEST_FOLD_IRIS + "missing" + fold + "WEKA.arff"),
                                              "label"));
            //the difference between a good and a bad fold can be quite big here dependin on RNG.
            //The average is still going to be quite close to .96 though
            assertEquals(auc.get(auc.size() - 1), 0.96, 0.1);
            aucSum += auc.get(auc.size() - 1);
        }
        double averageAUC = aucSum / folds.size();
        assertEquals(averageAUC, 0.96, 0.04);
        return averageAUC;
    }

    private double vertiBayesAsiaTest(String left, String right, Instances testData, String target) throws Exception {
        ExpectationMaximizationResponse response = generateModel(buildAsiaNetwork(), left, right, target);
        BayesNet network = response.getWeka();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);
        return eval.weightedAreaUnderROC();
    }

    private double vertiBayesAlarmTest(String left, String right, Instances testData, String target) throws Exception {
        ExpectationMaximizationResponse response = generateModel(buildAlarmNetwork(), left, right, target);
        BayesNet network = response.getWeka();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);
        return eval.weightedAreaUnderROC();
    }

    private double vertiBayesIrisTest(String left, String right, Instances testData, String target) throws Exception {
        ExpectationMaximizationResponse response = generateModel(buildIrisNetworkBinned(), left, right, target);
        BayesNet network = response.getWeka();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);
        return eval.weightedAreaUnderROC();
    }

    private double vertiBayesIrisMissingTest(String left, String right, Instances testData, String target)
            throws Exception {
        ExpectationMaximizationResponse response = generateModel(buildIrisNetworkBinnedMissing(), left, right, target);
        BayesNet network = response.getWeka();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);
        return eval.weightedAreaUnderROC();
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

        VertiBayesCentralServer central = new VertiBayesCentralServer();
        central.initEndpoints(Arrays.asList(endpoint1, endpoint2), secretEnd);
        return central;
    }
}
