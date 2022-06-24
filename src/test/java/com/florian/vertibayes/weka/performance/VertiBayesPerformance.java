package com.florian.vertibayes.weka.performance;

import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationResponse;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationTestResponse;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.weka.performance.tests.util.Performance;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;

import java.util.List;

import static com.florian.vertibayes.weka.performance.tests.util.Util.createCentral;
import static com.florian.vertibayes.weka.performance.tests.util.Util.generateSyntheticFold;

public class VertiBayesPerformance {
    public static Performance buildAndValidate(String left, String right, Instances testData,
                                               String target, String test, List<WebNode> nodes)
            throws Exception {
        ExpectationMaximizationTestResponse response = (ExpectationMaximizationTestResponse) generateModel(
                nodes, left, right, target);
        BayesNet network = response.getWeka();
        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(network, testData);

        Performance res = new Performance();
        res.setSyntheticAuc(response.getSyntheticAuc());
        res.setRealAuc(eval.weightedAreaUnderROC());
        res.setSyntheticFoldAuc(
                generateSyntheticFold(network, test, response.getNodes(), nodes, target));
        return res;
    }

    private static ExpectationMaximizationResponse generateModel(List<WebNode> input, String firsthalf,
                                                                 String secondhalf,
                                                                 String target)
            throws Exception {
        VertiBayesCentralServer central = createCentral(firsthalf, secondhalf);
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(input);
        req.setTarget(target);
        return central.expectationMaximization(req);
    }

}
