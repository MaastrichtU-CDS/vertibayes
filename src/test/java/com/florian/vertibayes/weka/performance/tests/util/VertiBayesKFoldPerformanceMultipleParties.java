package com.florian.vertibayes.weka.performance.tests.util;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.data.Data;
import com.florian.nscalarproduct.error.InvalidDataFormatException;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationResponse;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationWekaResponse;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.florian.nscalarproduct.data.Parser.parseData;
import static com.florian.vertibayes.util.PrintingPress.printCSV;
import static com.florian.vertibayes.weka.performance.tests.util.Score.calculateAIC;
import static com.florian.vertibayes.weka.performance.tests.util.Util.generateSyntheticFold;
import static com.florian.vertibayes.weka.performance.tests.util.Util.recordErrors;

public class VertiBayesKFoldPerformanceMultipleParties {
    private static double TEST_POPULATION = 10000;


    public static Performance buildAndValidateFold(String path, int parties, Instances testData,
                                                   String target, String test, List<WebNode> nodes,
                                                   double minPercentage, Instances fullData, int folds)
            throws Exception {
        ExpectationMaximizationWekaResponse response = (ExpectationMaximizationWekaResponse) generateModel(
                nodes, path, parties, target, minPercentage, folds);


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

    public static ExpectationMaximizationResponse generateModel(List<WebNode> input, String path, int parties,
                                                                String target, double minPercentage, int folds)
            throws Exception {


        Data d = null;
        try {
            d = parseData(path, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidDataFormatException e) {
            e.printStackTrace();
        }
        List<String> paths = createVerticalSplit(d, parties);
        VertiBayesCentralServer central = createCentral(paths);
        WebBayesNetwork req = new WebBayesNetwork();
        req.setMinPercentage(minPercentage);
        req.setNodes(input);
        req.setTarget(target);
        req.setWekaResponse(true);
        req.setFolds(folds);
        return central.expectationMaximization(req);
    }

    private static List<String> createVerticalSplit(Data d, int parties) {

        List<String> paths = new ArrayList<>();
        List<List<List<Attribute>>> data = new ArrayList<>();

        for (int i = 0; i < parties; i++) {
            paths.add(String.valueOf(i) + ".csv");
            List<List<Attribute>> list = new ArrayList<>();
            list.add(d.getData().get(d.getAttributeCollumn("ID")));
            data.add(list);
        }

        for (int i = 0; i < d.getData().size(); i++) {
            if (i == d.getAttributeCollumn("ID")) {
                continue;
            }
            data.get(i % parties).add(d.getData().get(i));
        }

        for (int i = 0; i < parties; i++) {
            Data list = new Data(0, -1, data.get(i));
            List<String> csv = new ArrayList<>();
            String s = "";
            for (int j = 0; j < list.getData().size(); j++) {
                if (s.length() > 0) {
                    s += ",";
                }
                s += list.getData().get(j).get(0).getType();
            }
            csv.add(s);
            s = "";
            for (int j = 0; j < list.getData().size(); j++) {
                if (s.length() > 0) {
                    s += ",";
                }
                s += list.getData().get(j).get(0).getAttributeName();
            }
            csv.add(s);
            for (int k = 0; k < list.getNumberOfIndividuals(); k++) {
                s = "";
                for (int j = 0; j < list.getData().size(); j++) {
                    if (s.length() > 0) {
                        s += ",";
                    }
                    s += list.getData().get(j).get(k).getValue();
                }
                csv.add(s);
            }
            printCSV(csv, paths.get(i));
        }
        return paths;

    }

    public static VertiBayesCentralServer createCentral(List<String> paths) {
        List<BayesServer> stations = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            stations.add(new BayesServer(paths.get(i), String.valueOf(i)));
        }

        List<ServerEndpoint> endpoints = new ArrayList<>();
        for (BayesServer s : stations) {
            endpoints.add(new VertiBayesEndpoint(s));
        }

        BayesServer secret = new BayesServer("secret", endpoints);

        ServerEndpoint secretEnd = new ServerEndpoint(secret);

        List<ServerEndpoint> all = new ArrayList<>();
        all.addAll(endpoints);
        all.add(secretEnd);

        secret.setEndpoints(all);
        for (BayesServer s : stations) {
            s.setEndpoints(all);
        }
        VertiBayesCentralServer central = new VertiBayesCentralServer(true);
        central.initEndpoints(endpoints, secretEnd);
        return central;
    }


}
