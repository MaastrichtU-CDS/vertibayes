package com.florian.vertibayes.weka.performance.tests.util;

import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationResponse;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationWekaResponse;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;

import java.util.List;

import static com.florian.vertibayes.weka.performance.tests.util.Score.calculateAIC;
import static com.florian.vertibayes.weka.performance.tests.util.Util.*;

public class VertiBayesKFoldPerformance {
    private static double TEST_POPULATION = 10000;

    public static Performance buildAndValidateFold(String left, String right, Instances testData,
                                                   String target, String test, List<WebNode> nodes,
                                                   double minPercentage, Instances fullData, int folds)
            throws Exception {
        ExpectationMaximizationWekaResponse response = (ExpectationMaximizationWekaResponse) generateModel(
                nodes, left, right, target, minPercentage, folds);


        BayesNet network = response.getWeka();
        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);

        Performance res = new Performance();
        res.setSyntheticAuc(response.getScvAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        generateSyntheticFold(network, test, response.getNodes(), nodes, target, minPercentage, res);
        res.setAIC(calculateAIC(fullData, network));
        res.setSvdgAuc(response.getSvdgAuc());

        res.getErrors().put(test, recordErrors(network, testData));
        return res;
    }

    public static ExpectationMaximizationResponse generateModel(List<WebNode> input, String firsthalf,
                                                                String secondhalf,
                                                                String target, double minPercentage, int folds)
            throws Exception {
        VertiBayesCentralServer central = createCentral(firsthalf, secondhalf);
        WebBayesNetwork req = new WebBayesNetwork();
        req.setMinPercentage(minPercentage);
        req.setNodes(input);
        req.setTarget(target);
        req.setWekaResponse(true);
        req.setFolds(folds);
        return central.expectationMaximization(req);
    }


}
