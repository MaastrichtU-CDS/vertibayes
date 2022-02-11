package com.florian.vertibayes.weka;

import com.florian.vertibayes.webservice.VertiBayesCentralServer;
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
import java.util.List;
import java.util.Random;

import static com.florian.vertibayes.notunittests.generatedata.GenerateData.createCentral;
import static com.florian.vertibayes.notunittests.generatedata.GenerateDataNoFolds.buildIrisNetworkBinned;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPerformance {
    private int FOLDS = 10;
    public static final String LEFTHALF_IRIS = "resources/Experiments/iris/generated/irisLeftSplit";
    public static final String IRIS = "resources/Experiments/iris/generated/iris";
    public static final String RIGHTHALF_IRIS = "resources/Experiments/iris/generated/irisRightSplit";
    public static final String LEFTHALF_IRIS_MISSING = "resources/Experiments/iris/generated/irismissingLeftSplit";
    public static final String RIGHTHALF_IRIS_MISSING = "resources/Experiments/iris/generated/irismissingRightSplit";

    public static final String first = "resources/Experiments/iris/iris_firsthalf.csv";
    public static final String second = "resources/Experiments/iris/iris_secondhalf.csv";
    public static final String weka = "resources/Experiments/iris/irisWeka2.arff";
    public static final String FIRSTHALF_IRIS = "resources/Experiments/iris/iris_firsthalf.csv";
    public static final String SECONDHALF_IRIS = "resources/Experiments/iris/iris_secondhalf.csv";

    // IMPORTANT TO NOTE; IF THESE TEST BEHAVE WEIRDLY MANUALLY CHECK IN WEKA.
    // ISSUES LIKE MISALIGNED COLLUMNS LEAD TO WEIRD RESULTS
    @Test
    public void vertiBayesTest() throws Exception {
        List<Integer> folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }

        test(folds);
    }

    @Test
    public void testVertiBayesFullDataSer() throws Exception {
        double auc = vertiBayesIrisTest(FIRSTHALF_IRIS, SECONDHALF_IRIS, readData("label", weka), "label");

        //this unit test should lead to overfitting as testset = trainingset and there are no k-folds or anything.
        //So performance should be high
        //However, due to the random factors there is some variance possible
        assertEquals(auc, 0.98, 0.025);
    }

    private void test(List<Integer> folds) throws Exception {
        List<Double> auc = new ArrayList<>();
//        for (Integer fold : folds) {
//            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
//            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
//                    .replace("]", "").replace(" ", "").replace(",", "");
//            String left = LEFTHALF_IRIS + ids + ".csv";
//            String right = RIGHTHALF_IRIS + ids + ".csv";
//            auc.add(vertiBayesIrisTest(left, right, readDataCSV("label", IRIS + fold + "WEKA.csv"), "label"));
//        }

        auc.add(vertiBayesIrisTest(FIRSTHALF_IRIS, SECONDHALF_IRIS, readData("label", weka), "label"));


        System.out.println(auc);
    }

    private double vertiBayesIrisTest(String left, String right, Instances testData, String target) throws Exception {
        ExpectationMaximizationResponse response = generateModel(buildIrisNetworkBinned(), left, right, target);
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


    private String wekaTest(String target, String biff, String arff) throws Exception {
        FromFile search = new FromFile();
        search.setBIFFile(biff);

        BayesNet network = new BayesNet();
        network.setSearchAlgorithm(search);
        Instances data = readData(target, arff);

        Evaluation eval = new Evaluation(data);
        network.buildClassifier(data);


        eval.crossValidateModel(network, data, 10, new Random(1));

        return eval.toClassDetailsString();

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
}
