package com.florian.vertibayes.notunittests.generatedata;

import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.BayesServer;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;
import com.florian.vertibayes.webservice.domain.MaximumLikelyhoodRequest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class GenerateData {
    private static final String CSV_PATH_IRIS = "resources/Experiments/generated/generatedDataIris.csv";
    private static final String CSV_PATH_IRIS_COMPLEX = "resources/Experiments/generated/generatedDataIrisComplex.csv";
    public static final String FIRSTHALF_IRIS = "resources/Experiments/iris/iris_firsthalf.csv";
    public static final String SECONDHALF_IRIS = "resources/Experiments/iris/iris_secondhalf.csv";

    private static final String CSV_PATH_IRIS_MISSING = "resources/Experiments/generated/generatedDataIrisMissing.csv";
    private static final String CSV_PATH_IRIS_MISSING_COMPLEX = "resources/Experiments/generated" +
            "/generatedDataIrisMissing_COMPLEX.csv";
    public static final String FIRSTHALF_IRIS_MISSING = "resources/Experiments/iris/irisMissingFirstHalf.csv";
    public static final String SECONDHALF_IRIS_MISSING = "resources/Experiments/iris/irisMissingSecondHalf.csv";

    private static final String CSV_PATH_ASIA = "resources/Experiments/generated/generatedDataAsia.csv";
    public static final String FIRSTHALF_ASIA = "resources/Experiments/asia/Asia10k_firstHalf.csv";
    public static final String SECONDHALF_ASIA = "resources/Experiments/asia/Asia10k_secondHalf.csv";

    private static final String CSV_PATH_ASIA_MISSING = "resources/Experiments/generated/generatedDataAsiaMissing.csv";
    public static final String FIRSTHALF_ASIA_MISSING = "resources/Experiments/asia/asiaMissingFirstHalf.csv";
    public static final String SECONDHALF_ASIA_MISSING = "resources/Experiments/asia/asiaMissingSecondHalf.csv";


    private static final String CSV_PATH_ALARM = "resources/Experiments/generated/generatedDataAlarm.csv";
    public static final String FIRSTHALF_ALARM = "resources/Experiments/alarm/Alarm10k_firsthalf.csv";
    public static final String SECONDHALF_ALARM = "resources/Experiments/alarm/Alarm10k_secondhalf.csv";

    private static final String CSV_PATH_ALARM_MISSING = "resources/Experiments/generated/generatedDataAlarmMissing" +
            ".csv";
    public static final String FIRSTHALF_ALARM_MISSING = "resources/Experiments/alarm/alarmMissingFirstHalf.csv";
    public static final String SECONDHALF_ALARM_MISSING = "resources/Experiments/alarm/alarmMissingSecondHalf.csv";


    @Test
    public void generateAllData() {
        //this is not a unittest, this exists purely to be able to generate data without needing to setup an entire
        // vantage6 infra or even several spring boot instances
        // Generating all 3 sets of data takes about 2 minutes with Alarm taking 99% of that time
        // With missing data it takes considerably longer, especially the "complex" iris network becomes hell
        // Needs binning

        generateData(buildIrisNetwork(), CSV_PATH_IRIS, FIRSTHALF_IRIS, SECONDHALF_IRIS, 150);
        generateData(buildIrisNetworkComplex(), CSV_PATH_IRIS_COMPLEX, FIRSTHALF_IRIS, SECONDHALF_IRIS, 150);
        generateData(buildAsiaNetwork(), CSV_PATH_ASIA, FIRSTHALF_ASIA, SECONDHALF_ASIA, 10000);
        generateData(buildAlarmNetwork(), CSV_PATH_ALARM, FIRSTHALF_ALARM, SECONDHALF_ALARM, 10000);

        generateData(buildIrisNetwork(), CSV_PATH_IRIS_MISSING, FIRSTHALF_IRIS_MISSING, SECONDHALF_IRIS_MISSING, 150);
        generateData(buildIrisNetworkComplex(), CSV_PATH_IRIS_MISSING_COMPLEX, FIRSTHALF_IRIS_MISSING,
                     SECONDHALF_IRIS_MISSING, 150);
        generateData(buildAsiaNetwork(), CSV_PATH_ASIA_MISSING, FIRSTHALF_ASIA_MISSING, SECONDHALF_ASIA_MISSING, 10000);
        generateData(buildAlarmNetwork(), CSV_PATH_ALARM_MISSING, FIRSTHALF_ALARM_MISSING, SECONDHALF_ALARM_MISSING,
                     10000);
    }


    private List<Node> buildAlarmNetwork() {
        Node mvs = createNode("MINVOLSET", Attribute.AttributeType.string, new ArrayList<>());
        Node vmch = createNode("VENTMACH", Attribute.AttributeType.string, Arrays.asList(mvs));
        Node disc = createNode("DISCONNECT", Attribute.AttributeType.string, new ArrayList<>());
        Node vtub = createNode("VENTTUBE", Attribute.AttributeType.string, Arrays.asList(disc, vmch));
        Node kink = createNode("KINKEDTUBE", Attribute.AttributeType.string, new ArrayList<>());
        Node pmb = createNode("PULMEMBOLUS", Attribute.AttributeType.string, new ArrayList<>());
        Node inT = createNode("INTUBATION", Attribute.AttributeType.string, new ArrayList<>());
        Node pap = createNode("PAP", Attribute.AttributeType.string, Arrays.asList(pmb));
        Node shnt = createNode("SHUNT", Attribute.AttributeType.string, Arrays.asList(pmb, inT));
        Node vlng = createNode("VENTLUNG", Attribute.AttributeType.string, Arrays.asList(inT, kink, vtub));
        Node prss = createNode("PRESS", Attribute.AttributeType.string, Arrays.asList(inT, kink, vtub));
        Node fio2 = createNode("FIO2", Attribute.AttributeType.string, new ArrayList<>());
        Node minv = createNode("MINVOL", Attribute.AttributeType.string, Arrays.asList(inT, vlng));
        Node valv = createNode("VENTALV", Attribute.AttributeType.string, Arrays.asList(inT, vlng));
        Node pvs = createNode("PVSAT", Attribute.AttributeType.string, Arrays.asList(fio2, valv));
        Node aco2 = createNode("ARTCO2", Attribute.AttributeType.string, Arrays.asList(valv));
        Node sao2 = createNode("SAO2", Attribute.AttributeType.string, Arrays.asList(shnt, pvs));
        Node eco2 = createNode("EXPCO2", Attribute.AttributeType.string, Arrays.asList(aco2, vlng));
        Node apl = createNode("ANAPHYLAXIS", Attribute.AttributeType.string, new ArrayList<>());
        Node anes = createNode("INSUFFANESTH", Attribute.AttributeType.string, new ArrayList<>());
        Node tpr = createNode("TPR", Attribute.AttributeType.string, Arrays.asList(apl));
        Node cchl = createNode("CATECHOL", Attribute.AttributeType.string, Arrays.asList(tpr, sao2, aco2, anes));
        Node lvf = createNode("LVFAILURE", Attribute.AttributeType.string, new ArrayList<>());
        Node hyp = createNode("HYPOVOLEMIA", Attribute.AttributeType.string, new ArrayList<>());
        Node hist = createNode("HISTORY", Attribute.AttributeType.string, Arrays.asList(lvf));
        Node lvv = createNode("LVEDVOLUME", Attribute.AttributeType.string, Arrays.asList(lvf, hyp));
        Node erlo = createNode("ERRLOWOUTPUT", Attribute.AttributeType.string, new ArrayList<>());
        Node stkv = createNode("STROKEVOLUME", Attribute.AttributeType.string, Arrays.asList(lvf, hyp));
        Node hr = createNode("HR", Attribute.AttributeType.string, Arrays.asList(cchl));
        Node erca = createNode("ERRCAUTER", Attribute.AttributeType.string, new ArrayList<>());
        Node cvp = createNode("CVP", Attribute.AttributeType.string, Arrays.asList(lvv));
        Node pcwp = createNode("PCWP", Attribute.AttributeType.string, Arrays.asList(lvv));
        Node hrbp = createNode("HRBP", Attribute.AttributeType.string, Arrays.asList(erlo, hr));
        Node co = createNode("CO", Attribute.AttributeType.string, Arrays.asList(stkv, hr));
        Node hrsa = createNode("HRSAT", Attribute.AttributeType.string, Arrays.asList(hr, erca));
        Node hrek = createNode("HREKG", Attribute.AttributeType.string, Arrays.asList(hr, erca));
        Node bp = createNode("BP", Attribute.AttributeType.string, Arrays.asList(tpr, co));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(hist, cvp, pcwp, hyp, lvv, lvf, stkv, erlo, hrbp, hrek, erca, hrsa, anes, apl, tpr, eco2,
                             kink, minv, fio2, pvs, sao2, pap, pmb, shnt, inT, prss, disc, mvs, vmch, vtub,
                             vlng, valv, aco2, cchl, hr, co, bp);
    }


    private List<Node> buildIrisNetwork() {
        Node label = createNode("label", Attribute.AttributeType.string, new ArrayList<>());
        Node petallength = createNode("petallength", Attribute.AttributeType.number, Arrays.asList(label));
        Node petalwidth = createNode("petalwidth", Attribute.AttributeType.number, Arrays.asList(label));
        Node sepallength = createNode("sepallength", Attribute.AttributeType.number, Arrays.asList(label));
        Node sepalwidth = createNode("sepalwidth", Attribute.AttributeType.number, Arrays.asList(label));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    private List<Node> buildIrisNetworkComplex() {
        Node label = createNode("label", Attribute.AttributeType.string, new ArrayList<>());
        Node petallength = createNode("petallength", Attribute.AttributeType.number, Arrays.asList(label));
        Node petalwidth = createNode("petalwidth", Attribute.AttributeType.number, Arrays.asList(label, petallength));
        Node sepallength = createNode("sepallength", Attribute.AttributeType.number, Arrays.asList(label));
        Node sepalwidth = createNode("sepalwidth", Attribute.AttributeType.number, Arrays.asList(label));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    private List<Node> buildAsiaNetwork() {
        Node asia = createNode("asia", Attribute.AttributeType.string, new ArrayList<>());
        Node tub = createNode("tub", Attribute.AttributeType.string, Arrays.asList(asia));
        Node smoke = createNode("smoke", Attribute.AttributeType.string, new ArrayList<>());
        Node lung = createNode("lung", Attribute.AttributeType.string, Arrays.asList(smoke));
        Node bronc = createNode("bronc", Attribute.AttributeType.string, Arrays.asList(smoke));
        Node either = createNode("either", Attribute.AttributeType.string, Arrays.asList(tub, lung));
        Node xray = createNode("xray", Attribute.AttributeType.string, Arrays.asList(either));
        Node dysp = createNode("dysp", Attribute.AttributeType.string, Arrays.asList(either, bronc));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(asia, tub, smoke, lung, bronc, either, xray, dysp);
    }

    private Node createNode(String name, Attribute.AttributeType type, List<Node> parents) {
        Node n = new Node();
        n.setType(type);
        n.setName(name);
        n.setParents(parents);
        return n;
    }

    private void generateData(List<Node> nodes, String output, String firsthalf, String secondhalf, int samplesize) {
        //utility function to generate data locally without needing to create an entire vantage6 setup
        //easier for experiments
        VertiBayesCentralServer central = createCentral(firsthalf, secondhalf);
        MaximumLikelyhoodRequest req = new MaximumLikelyhoodRequest();
        req.setNodes(nodes);
        central.maximumLikelyhood(req);

        //select a root
        Node root = null;
        for (Node n : nodes) {
            if (n.getParents().size() == 0) {
                root = n;
            }
        }
        //set child relationship
        for (Node n : nodes) {
            for (Node p : n.getParents()) {
                p.getChildren().add(n);
            }
        }
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
                                individual.put(node.getName(), theta.getLocalValue().getValue());
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
                                } else if (individual.get(parent.getName()) != parent.getValue().getValue()) {
                                    //A parent has the wrong value for this theta, move on
                                    correctTheta = false;
                                    break;
                                }
                            }
                            if (correctTheta) {
                                y += theta.getP();
                                if (x <= y) {
                                    individual.put(node.getName(), theta.getLocalValue().getValue());
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