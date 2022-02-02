package com.florian.vertibayes.notunittests.generatedata;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.domain.external.WebNode;
import com.florian.vertibayes.webservice.mapping.WebNodeMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateData {

    public static final String LEFTHALF_IRIS = "resources/Experiments/iris/folds/irisLeftSplit";
    public static final String RIGHTHALF_IRIS = "resources/Experiments/iris/folds/irisRightSplit";
    public static final String LEFTHALF_IRIS_MISSING = "resources/Experiments/iris/folds/irismissingLeftSplit";
    public static final String RIGHTHALF_IRIS_MISSING = "resources/Experiments/iris/folds/irismissingRightSplit";
    private static final String CSV_PATH_IRIS_GENERATED = "resources/Experiments/generated/generatedDataIris";

    public static final String LEFTHALF_ASIA = "resources/Experiments/asia/folds/asiaLeftSplit";
    public static final String RIGHTHALF_ASIA = "resources/Experiments/asia/folds/asiaRightSplit";
    public static final String LEFTHALF_ASIA_MISSING = "resources/Experiments/asia/folds/asiamissingLeftSplit";
    public static final String RIGHTHALF_ASIA_MISSING = "resources/Experiments/asia/folds/asiamissingRightSplit";
    private static final String CSV_PATH_ASIA_GENERATED = "resources/Experiments/generated/generatedDataAsia";

    public static final String LEFTHALF_ALARM = "resources/Experiments/alarm/folds/alarmLeftSplit";
    public static final String RIGHTHALF_ALARM = "resources/Experiments/alarm/folds/alarmRightSplit";
    public static final String LEFTHALF_ALARM_MISSING = "resources/Experiments/alarm/folds/alarmmissingLeftSplit";
    public static final String RIGHTHALF_ALARM_MISSING = "resources/Experiments/alarm/folds/alarmmissingRightSplit";
    private static final String CSV_PATH_ALARM_GENERATED = "resources/Experiments/generated/generatedDataAlarm";

    private static final Integer FOLDS = 10;

    @Test
    public void generateAllData() {
        //this is not a unittest, this exists purely to be able to generate data without needing to setup an entire
        // vantage6 infra or even several spring boot instances
        // Generating all 3 sets of data takes about 2 minutes with Alarm taking 99% of that time
        // With missing data it takes considerably longer
        // With folds this becomes even worse given that there are 10 folds.

        List<Integer> folds = new ArrayList<>();
        for (int i = 0; i < FOLDS; i++) {
            folds.add(i);
        }

        generateIRIS(folds);
        generateAsia(folds);
        generateAlarm(folds);
    }

    private void generateIRIS(List<Integer> folds) {
        // no missing data
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = LEFTHALF_IRIS + ids + ".csv";
            String right = RIGHTHALF_IRIS + ids + ".csv";
            String generated = CSV_PATH_IRIS_GENERATED + ids + ".csv";
            generateData(buildIrisNetwork(), generated, left, right, 150);
            break;
        }
        //missing data
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = LEFTHALF_IRIS_MISSING + ids + ".csv";
            String right = RIGHTHALF_IRIS_MISSING + ids + ".csv";
            String generated = CSV_PATH_IRIS_GENERATED + ids + "MISSING.csv";
            generateData(buildIrisNetwork(), generated, left, right, 150);
            break;
        }
    }

    private void generateAlarm(List<Integer> folds) {
        // no missing data
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = LEFTHALF_ALARM + ids + ".csv";
            String right = RIGHTHALF_ALARM + ids + ".csv";
            String generated = CSV_PATH_ALARM_GENERATED + ids + ".csv";
            generateData(buildAlarmNetwork(), generated, left, right, 10000);
            break;
        }
        //missing data
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = LEFTHALF_ALARM_MISSING + ids + ".csv";
            String right = RIGHTHALF_ALARM_MISSING + ids + ".csv";
            String generated = CSV_PATH_ALARM_GENERATED + ids + "MISSING.csv";
            generateData(buildAlarmNetwork(), generated, left, right, 1000);
            break;
        }
    }

    private void generateAsia(List<Integer> folds) {
        // no missing data
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = LEFTHALF_ASIA + ids + ".csv";
            String right = RIGHTHALF_ASIA + ids + ".csv";
            String generated = CSV_PATH_ASIA_GENERATED + ids + ".csv";
            generateData(buildAsiaNetwork(), generated, left, right, 1000);
            break;
        }
        //missing data
        for (Integer fold : folds) {
            List<Integer> otherFolds = folds.stream().filter(x -> x != fold).collect(Collectors.toList());
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");
            String left = LEFTHALF_ASIA_MISSING + ids + ".csv";
            String right = RIGHTHALF_ASIA_MISSING + ids + ".csv";
            String generated = CSV_PATH_ASIA_GENERATED + ids + "MISSING.csv";
            generateData(buildAsiaNetwork(), generated, left, right, 1000);
            break;
        }
    }


    private List<WebNode> buildAlarmNetwork() {
        WebNode mvs = createWebNode("MINVOLSET", Attribute.AttributeType.string, new ArrayList<>());
        WebNode vmch = createWebNode("VENTMACH", Attribute.AttributeType.string, Arrays.asList(mvs.getName()));
        WebNode disc = createWebNode("DISCONNECT", Attribute.AttributeType.string, new ArrayList<>());
        WebNode vtub = createWebNode("VENTTUBE", Attribute.AttributeType.string,
                                     Arrays.asList(disc.getName(), vmch.getName()));
        WebNode kink = createWebNode("KINKEDTUBE", Attribute.AttributeType.string, new ArrayList<>());
        WebNode pmb = createWebNode("PULMEMBOLUS", Attribute.AttributeType.string, new ArrayList<>());
        WebNode inT = createWebNode("INTUBATION", Attribute.AttributeType.string, new ArrayList<>());
        WebNode pap = createWebNode("PAP", Attribute.AttributeType.string, Arrays.asList(pmb.getName()));
        WebNode shnt = createWebNode("SHUNT", Attribute.AttributeType.string,
                                     Arrays.asList(pmb.getName(), inT.getName()));
        WebNode vlng = createWebNode("VENTLUNG", Attribute.AttributeType.string,
                                     Arrays.asList(inT.getName(), kink.getName(), vtub.getName()));
        WebNode prss = createWebNode("PRESS", Attribute.AttributeType.string,
                                     Arrays.asList(inT.getName(), kink.getName(), vtub.getName()));
        WebNode fio2 = createWebNode("FIO2", Attribute.AttributeType.string, new ArrayList<>());
        WebNode minv = createWebNode("MINVOL", Attribute.AttributeType.string,
                                     Arrays.asList(inT.getName(), vlng.getName()));
        WebNode valv = createWebNode("VENTALV", Attribute.AttributeType.string,
                                     Arrays.asList(inT.getName(), vlng.getName()));
        WebNode pvs = createWebNode("PVSAT", Attribute.AttributeType.string,
                                    Arrays.asList(fio2.getName(), valv.getName()));
        WebNode aco2 = createWebNode("ARTCO2", Attribute.AttributeType.string, Arrays.asList(valv.getName()));
        WebNode sao2 = createWebNode("SAO2", Attribute.AttributeType.string,
                                     Arrays.asList(shnt.getName(), pvs.getName()));
        WebNode eco2 = createWebNode("EXPCO2", Attribute.AttributeType.string,
                                     Arrays.asList(aco2.getName(), vlng.getName()));
        WebNode apl = createWebNode("ANAPHYLAXIS", Attribute.AttributeType.string, new ArrayList<>());
        WebNode anes = createWebNode("INSUFFANESTH", Attribute.AttributeType.string, new ArrayList<>());
        WebNode tpr = createWebNode("TPR", Attribute.AttributeType.string, Arrays.asList(apl.getName()));
        WebNode cchl = createWebNode("CATECHOL", Attribute.AttributeType.string,
                                     Arrays.asList(tpr.getName(), sao2.getName(), aco2.getName(), anes.getName()));
        WebNode lvf = createWebNode("LVFAILURE", Attribute.AttributeType.string, new ArrayList<>());
        WebNode hyp = createWebNode("HYPOVOLEMIA", Attribute.AttributeType.string, new ArrayList<>());
        WebNode hist = createWebNode("HISTORY", Attribute.AttributeType.string, Arrays.asList(lvf.getName()));
        WebNode lvv = createWebNode("LVEDVOLUME", Attribute.AttributeType.string,
                                    Arrays.asList(lvf.getName(), hyp.getName()));
        WebNode erlo = createWebNode("ERRLOWOUTPUT", Attribute.AttributeType.string, new ArrayList<>());
        WebNode stkv = createWebNode("STROKEVOLUME", Attribute.AttributeType.string,
                                     Arrays.asList(lvf.getName(), hyp.getName()));
        WebNode hr = createWebNode("HR", Attribute.AttributeType.string, Arrays.asList(cchl.getName()));
        WebNode erca = createWebNode("ERRCAUTER", Attribute.AttributeType.string, new ArrayList<>());
        WebNode cvp = createWebNode("CVP", Attribute.AttributeType.string, Arrays.asList(lvv.getName()));
        WebNode pcwp = createWebNode("PCWP", Attribute.AttributeType.string, Arrays.asList(lvv.getName()));
        WebNode hrbp = createWebNode("HRBP", Attribute.AttributeType.string,
                                     Arrays.asList(erlo.getName(), hr.getName()));
        WebNode co = createWebNode("CO", Attribute.AttributeType.string, Arrays.asList(stkv.getName(), hr.getName()));
        WebNode hrsa = createWebNode("HRSAT", Attribute.AttributeType.string,
                                     Arrays.asList(hr.getName(), erca.getName()));
        WebNode hrek = createWebNode("HREKG", Attribute.AttributeType.string,
                                     Arrays.asList(hr.getName(), erca.getName()));
        WebNode bp = createWebNode("BP", Attribute.AttributeType.string, Arrays.asList(tpr.getName(), co.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(hist, cvp, pcwp, hyp, lvv, lvf, stkv, erlo, hrbp, hrek, erca, hrsa, anes, apl, tpr, eco2,
                             kink, minv, fio2, pvs, sao2, pap, pmb, shnt, inT, prss, disc, mvs, vmch, vtub,
                             vlng, valv, aco2, cchl, hr, co, bp);
    }


    public static List<WebNode> buildIrisNetwork() {
        WebNode label = createWebNode("label", Attribute.AttributeType.string, new ArrayList<>());
        WebNode petallength = createWebNode("petallength", Attribute.AttributeType.number,
                                            Arrays.asList(label.getName()));
        WebNode petalwidth = createWebNode("petalwidth", Attribute.AttributeType.number,
                                           Arrays.asList(label.getName()));
        WebNode sepallength = createWebNode("sepallength", Attribute.AttributeType.number,
                                            Arrays.asList(label.getName()));
        WebNode sepalwidth = createWebNode("sepalwidth", Attribute.AttributeType.number,
                                           Arrays.asList(label.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    private List<WebNode> buildIrisNetworkComplex() {
        WebNode label = createWebNode("label", Attribute.AttributeType.string, new ArrayList<>());
        WebNode petallength = createWebNode("petallength", Attribute.AttributeType.number,
                                            Arrays.asList(label.getName()));
        WebNode petalwidth = createWebNode("petalwidth", Attribute.AttributeType.number,
                                           Arrays.asList(label.getName(), petallength.getName()));
        WebNode sepallength = createWebNode("sepallength", Attribute.AttributeType.number,
                                            Arrays.asList(label.getName()));
        WebNode sepalwidth = createWebNode("sepalwidth", Attribute.AttributeType.number,
                                           Arrays.asList(label.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    private List<WebNode> buildAsiaNetwork() {
        WebNode asia = createWebNode("asia", Attribute.AttributeType.string, new ArrayList<>());
        WebNode tub = createWebNode("tub", Attribute.AttributeType.string, Arrays.asList(asia.getName()));
        WebNode smoke = createWebNode("smoke", Attribute.AttributeType.string, new ArrayList<>());
        WebNode lung = createWebNode("lung", Attribute.AttributeType.string, Arrays.asList(smoke.getName()));
        WebNode bronc = createWebNode("bronc", Attribute.AttributeType.string, Arrays.asList(smoke.getName()));
        WebNode either = createWebNode("either", Attribute.AttributeType.string,
                                       Arrays.asList(tub.getName(), lung.getName()));
        WebNode xray = createWebNode("xray", Attribute.AttributeType.string, Arrays.asList(either.getName()));
        WebNode dysp = createWebNode("dysp", Attribute.AttributeType.string,
                                     Arrays.asList(either.getName(), bronc.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(asia, tub, smoke, lung, bronc, either, xray, dysp);
    }

    private static WebNode createWebNode(String name, Attribute.AttributeType type, List<String> parents) {
        WebNode n = new WebNode();
        n.setType(type);
        n.setName(name);
        n.setParents(parents);
        return n;
    }

    private void generateData(List<WebNode> input, String output, String firsthalf, String secondhalf, int samplesize) {
        //utility function to generate data locally without needing to create an entire vantage6 setup
        //easier for experiments
        VertiBayesCentralServer central = createCentral(firsthalf, secondhalf);
        WebBayesNetwork req = new WebBayesNetwork();
        req.setNodes(input);
        List<Node> nodes = WebNodeMapper.mapWebNodeToNode(central.maximumLikelyhood(req).getNodes());

        List<String> data = new ArrayList<>();

        String types = "";
        String names = "";
        int j = 0;
        for (Node node : nodes) {
            if (j > 0) {
                types += ",";
                names += ",";
            }
            j++;
            types += node.getType();
            names += node.getName();
        }
        data.add(types);
        data.add(names);
        for (int i = 0; i < samplesize; i++) {
            data.add(generateIndividual(nodes));
        }
        printCSV(data, output);
    }

    private void printCSV(List<String> data, String path) {
        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            data.stream()
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private String generateIndividual(List<Node> nodes) {
        Map<String, String> individual = new HashMap<>();
        boolean done = false;
        Random random = new Random();
        while (!done) {
            done = true;
            for (Node node : nodes) {
                if (individual.get(node.getName()) == null) {
                    done = false;
                    //this attribute does not have a value yet
                    double x = random.nextDouble();
                    double y = 0;
                    for (Theta theta : node.getProbabilities()) {
                        if (node.getParents().size() == 0) {
                            //no parents, just select a random value
                            y += theta.getP();
                            if (x <= y) {
                                AttributeRequirement local = theta.getLocalRequirement();
                                if (!local.isRange()) {
                                    individual.put(node.getName(), local.getValue().getValue());
                                } else {
                                    //generate a number from the range
                                    Double upper = Double.valueOf(local.getUpperLimit().getValue());
                                    Double lower = Double.valueOf(local.getLowerLimit().getValue());
                                    Double generated = random.nextDouble() * (upper - lower) + lower;
                                    individual.put(node.getName(), String.valueOf(generated));
                                }
                                break;
                            }
                        } else {
                            //node has parents, so check if parent values have been selected yet
                            boolean correctTheta = true;
                            for (ParentValue parent : theta.getParents()) {
                                if (individual.get(parent.getName()) == null) {
                                    //not all parents are selected, move on
                                    correctTheta = false;
                                    break;
                                } else {
                                    Attribute a = new Attribute(parent.getRequirement().getValue().getType(),
                                                                individual.get(parent.getName()), parent.getName());
                                    if (!parent.getRequirement().checkRequirement(
                                            a)) {
                                        //A parent has the wrong value for this theta, move on
                                        correctTheta = false;
                                        break;
                                    }

                                }
                            }
                            if (correctTheta) {
                                y += theta.getP();
                                if (x <= y) {
                                    AttributeRequirement local = theta.getLocalRequirement();
                                    if (!local.isRange()) {
                                        individual.put(node.getName(), local.getValue().getValue());
                                    } else {
                                        //generate a number from the range
                                        Double upper = Double.valueOf(local.getUpperLimit().getValue());
                                        Double lower = Double.valueOf(local.getLowerLimit().getValue());
                                        Double generated = random.nextDouble() * (upper - lower) + lower;
                                        individual.put(node.getName(), String.valueOf(generated));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        String s = "";
        int i = 0;
        for (Node node : nodes) {
            if (i > 0) {
                s += ",";
            }
            i++;
            s += individual.get(node.getName());
        }

        return s;
    }

    private VertiBayesCentralServer createCentral(String firsthalf, String secondhalf) {
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