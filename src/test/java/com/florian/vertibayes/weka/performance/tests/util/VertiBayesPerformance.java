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

public class VertiBayesPerformance {
    private static double TEST_POPULATION = 10000;

    public static Performance buildAndValidate(String left, String right, Instances testData,
                                               String target, String test, List<WebNode> nodes,
                                               double minPercentage, Instances fullData)
            throws Exception {
        ExpectationMaximizationWekaResponse response = (ExpectationMaximizationWekaResponse) generateModel(
                nodes, left, right, target, minPercentage);


        BayesNet network = response.getWeka();
        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);

        Performance res = new Performance();
        res.setSyntheticAuc(response.getScvAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        generateSyntheticFold(network, test, response.getNodes(), nodes, target, minPercentage, res);
        res.setAIC(calculateAIC(fullData, network));

        res.getErrors().put(test, recordErrors(network, testData));
        return res;
    }

    public static ExpectationMaximizationResponse generateModel(List<WebNode> input, String firsthalf,
                                                                String secondhalf,
                                                                String target, double minPercentage)
            throws Exception {
        VertiBayesCentralServer central = createCentral(firsthalf, secondhalf);
        WebBayesNetwork req = new WebBayesNetwork();
        req.setMinPercentage(minPercentage);
        req.setNodes(input);
        req.setTarget(target);
        req.setWekaResponse(true);
        return central.expectationMaximization(req);
    }


}
