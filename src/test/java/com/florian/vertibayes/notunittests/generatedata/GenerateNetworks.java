package com.florian.vertibayes.notunittests.generatedata;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.webservice.domain.external.WebNode;

import java.util.*;

public class GenerateNetworks {
    public static List<WebNode> buildAlarmNetwork() {
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

    public static List<WebNode> buildDiabetesNetwork() {
        WebNode outcome = createWebNode("Outcome", Attribute.AttributeType.string, new ArrayList<>());
        WebNode glucose = createWebNode("Glucose", Attribute.AttributeType.numeric, Arrays.asList(outcome.getName()));
        WebNode bloodPressure = createWebNode("BloodPressure", Attribute.AttributeType.numeric,
                                              Arrays.asList(outcome.getName()));
        WebNode skinThickness = createWebNode("SkinThickness", Attribute.AttributeType.numeric,
                                              Arrays.asList(outcome.getName()));
        WebNode diabetesPedigreeFunction = createWebNode("DiabetesPedigreeFunction", Attribute.AttributeType.real,
                                                         Arrays.asList(outcome.getName()));
        WebNode insulin = createWebNode("Insulin", Attribute.AttributeType.numeric,
                                        Arrays.asList(outcome.getName(), glucose.getName()));
        WebNode pregnancies = createWebNode("Pregnancies", Attribute.AttributeType.numeric,
                                            Arrays.asList(outcome.getName()));
        WebNode bmi = createWebNode("BMI", Attribute.AttributeType.real,
                                    Arrays.asList(outcome.getName(), insulin.getName()));
        WebNode age = createWebNode("Age", Attribute.AttributeType.numeric,
                                    Arrays.asList(outcome.getName(), insulin.getName(), pregnancies.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(pregnancies, glucose, bloodPressure, skinThickness, insulin, bmi, diabetesPedigreeFunction,
                             age, outcome);
    }

    public static List<WebNode> buildDiabetesNetworkDiscrete() {
        WebNode outcome = createWebNode("Outcome", Attribute.AttributeType.string, new ArrayList<>());
        WebNode glucose = createWebNode("Glucose", Attribute.AttributeType.string, Arrays.asList(outcome.getName()));
        WebNode bloodPressure = createWebNode("BloodPressure", Attribute.AttributeType.string,
                                              Arrays.asList(outcome.getName()));
        WebNode skinThickness = createWebNode("SkinThickness", Attribute.AttributeType.string,
                                              Arrays.asList(outcome.getName()));
        WebNode diabetesPedigreeFunction = createWebNode("DiabetesPedigreeFunction", Attribute.AttributeType.string,
                                                         Arrays.asList(outcome.getName()));
        WebNode insulin = createWebNode("Insulin", Attribute.AttributeType.string,
                                        Arrays.asList(outcome.getName(), glucose.getName()));
        WebNode pregnancies = createWebNode("Pregnancies", Attribute.AttributeType.string,
                                            Arrays.asList(outcome.getName()));
        WebNode bmi = createWebNode("BMI", Attribute.AttributeType.string,
                                    Arrays.asList(outcome.getName(), insulin.getName()));
        WebNode age = createWebNode("Age", Attribute.AttributeType.string,
                                    Arrays.asList(outcome.getName(), insulin.getName(), pregnancies.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(pregnancies, glucose, bloodPressure, skinThickness, insulin, bmi, diabetesPedigreeFunction,
                             age, outcome);
    }

    public static List<WebNode> buildIrisNetworkDiscrete() {
        WebNode label = createWebNode("label", Attribute.AttributeType.string, new ArrayList<>());
        WebNode petallength = createWebNode("petallength", Attribute.AttributeType.string,
                                            Arrays.asList(label.getName()));
        WebNode petalwidth = createWebNode("petalwidth", Attribute.AttributeType.string,
                                           Arrays.asList(label.getName()));
        WebNode sepallength = createWebNode("sepallength", Attribute.AttributeType.string,
                                            Arrays.asList(label.getName()));
        WebNode sepalwidth = createWebNode("sepalwidth", Attribute.AttributeType.string,
                                           Arrays.asList(label.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    public static List<WebNode> buildIrisNetworkNoBins() {
        WebNode label = createWebNode("label", Attribute.AttributeType.string, new ArrayList<>());
        WebNode petallength = createWebNode("petallength", Attribute.AttributeType.real,
                                            Arrays.asList(label.getName()));
        WebNode petalwidth = createWebNode("petalwidth", Attribute.AttributeType.real,
                                           Arrays.asList(label.getName()));
        WebNode sepallength = createWebNode("sepallength", Attribute.AttributeType.real,
                                            Arrays.asList(label.getName()));
        WebNode sepalwidth = createWebNode("sepalwidth", Attribute.AttributeType.real,
                                           Arrays.asList(label.getName()));

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    public static List<WebNode> buildIrisNetworkWekaBinned() {
        WebNode label = createWebNode("label", Attribute.AttributeType.string, new ArrayList<>());
        WebNode petallength = createWebNode("petallength", Attribute.AttributeType.real,
                                            Arrays.asList(label.getName()));
        WebNode petalwidth = createWebNode("petalwidth", Attribute.AttributeType.real,
                                           Arrays.asList(label.getName()));
        WebNode sepallength = createWebNode("sepallength", Attribute.AttributeType.real,
                                            Arrays.asList(label.getName()));
        WebNode sepalwidth = createWebNode("sepalwidth", Attribute.AttributeType.real,
                                           Arrays.asList(label.getName()));
        Set<Bin> sepalLenghtBins = new HashSet<>();
        Bin bin1 = new Bin();
        bin1.setLowerLimit("-inf");
        bin1.setUpperLimit("5.55");
        sepalLenghtBins.add(bin1);

        Bin bin2 = new Bin();
        bin2.setLowerLimit("5.55");
        bin2.setUpperLimit("6.15");
        sepalLenghtBins.add(bin2);

        Bin bin3 = new Bin();
        bin3.setLowerLimit("6.15");
        bin3.setUpperLimit("inf");
        sepalLenghtBins.add(bin3);

        sepallength.setBins(sepalLenghtBins);
        Set<Bin> sepalWidthBins = new HashSet<>();

        Bin bin4 = new Bin();
        bin4.setLowerLimit("-inf");
        bin4.setUpperLimit("2.95");
        sepalWidthBins.add(bin4);

        Bin bin5 = new Bin();
        bin5.setLowerLimit("2.95");
        bin5.setUpperLimit("3.35");
        sepalWidthBins.add(bin5);

        Bin bin6 = new Bin();
        bin6.setLowerLimit("3.35");
        bin6.setUpperLimit("inf");
        sepalWidthBins.add(bin6);

        sepalwidth.setBins(sepalWidthBins);
        Set<Bin> petalLengthbins = new HashSet<>();

        Bin bin7 = new Bin();
        bin7.setLowerLimit("-inf");
        bin7.setUpperLimit("2.45");
        petalLengthbins.add(bin7);

        Bin bin8 = new Bin();
        bin8.setLowerLimit("2.45");
        bin8.setUpperLimit("4.75");
        petalLengthbins.add(bin8);

        Bin bin9 = new Bin();
        bin9.setLowerLimit("4.75");
        bin9.setUpperLimit("inf");
        petalLengthbins.add(bin9);

        petallength.setBins(petalLengthbins);
        Set<Bin> petalWidthbins = new HashSet<>();
        Bin bin10 = new Bin();
        bin10.setLowerLimit("-inf");
        bin10.setUpperLimit("0.8");
        petalWidthbins.add(bin10);

        Bin bin11 = new Bin();
        bin11.setLowerLimit("0.8");
        bin11.setUpperLimit("1.75");
        petalWidthbins.add(bin11);

        Bin bin12 = new Bin();
        bin12.setLowerLimit("1.75");
        bin12.setUpperLimit("inf");
        petalWidthbins.add(bin12);

        petalwidth.setBins(petalWidthbins);

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }


    public static List<WebNode> buildIrisNetworkBinned() {
        WebNode label = createWebNode("label", Attribute.AttributeType.string, new ArrayList<>());
        WebNode petallength = createWebNode("petallength", Attribute.AttributeType.real,
                                            Arrays.asList(label.getName()));
        WebNode petalwidth = createWebNode("petalwidth", Attribute.AttributeType.real,
                                           Arrays.asList(label.getName()));
        WebNode sepallength = createWebNode("sepallength", Attribute.AttributeType.real,
                                            Arrays.asList(label.getName()));
        WebNode sepalwidth = createWebNode("sepalwidth", Attribute.AttributeType.real,
                                           Arrays.asList(label.getName()));
        Set<Bin> bins = new HashSet<>();
        for (double i = 4.3; i < 7.8; i += 0.1) {
            Bin bin = new Bin();
            bin.setLowerLimit(String.valueOf(i));
            bin.setUpperLimit(String.valueOf(i + 0.1));
            bins.add(bin);
        }
        sepallength.setBins(bins);
        bins = new HashSet<>();
        for (double i = 2; i < 4.3; i += 0.1) {
            Bin bin = new Bin();
            bin.setLowerLimit(String.valueOf(i));
            bin.setUpperLimit(String.valueOf(i + 0.1));
            bins.add(bin);
        }
        sepalwidth.setBins(bins);
        bins = new HashSet<>();
        for (double i = 1; i < 6.8; i += 0.1) {
            Bin bin = new Bin();
            bin.setLowerLimit(String.valueOf(i));
            bin.setUpperLimit(String.valueOf(i + 0.1));
            bins.add(bin);
        }
        petallength.setBins(bins);
        bins = new HashSet<>();
        for (double i = 0.1; i < 2.4; i += 0.1) {
            Bin bin = new Bin();
            bin.setLowerLimit(String.valueOf(i));
            bin.setUpperLimit(String.valueOf(i + 0.1));
            bins.add(bin);
        }
        petalwidth.setBins(bins);

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    public static List<WebNode> buildIrisNetworkBinnedMissing() {
        WebNode label = createWebNode("label", Attribute.AttributeType.string, new ArrayList<>());
        WebNode petallength = createWebNode("petallength", Attribute.AttributeType.real,
                                            Arrays.asList(label.getName()));
        WebNode petalwidth = createWebNode("petalwidth", Attribute.AttributeType.real,
                                           Arrays.asList(label.getName()));
        WebNode sepallength = createWebNode("sepallength", Attribute.AttributeType.real,
                                            Arrays.asList(label.getName()));
        WebNode sepalwidth = createWebNode("sepalwidth", Attribute.AttributeType.real,
                                           Arrays.asList(label.getName()));
        Set<Bin> bins = new HashSet<>();
        Bin missing = new Bin();
        missing.setUpperLimit("?");
        missing.setLowerLimit("?");
        bins.add(missing);
        for (double i = 4.3; i < 7.8; i += 0.1) {
            Bin bin = new Bin();
            bin.setLowerLimit(String.valueOf(i));
            bin.setUpperLimit(String.valueOf(i + 0.1));
            bins.add(bin);
        }

        sepallength.setBins(bins);
        bins = new HashSet<>();
        bins.add(missing);
        for (double i = 2; i < 2.3; i += 0.1) {
            Bin bin = new Bin();
            bin.setLowerLimit(String.valueOf(i));
            bin.setUpperLimit(String.valueOf(i + 0.1));
            bins.add(bin);
        }
        sepalwidth.setBins(bins);
        bins = new HashSet<>();
        bins.add(missing);
        for (double i = 1; i < 6.8; i += 0.1) {
            Bin bin = new Bin();
            bin.setLowerLimit(String.valueOf(i));
            bin.setUpperLimit(String.valueOf(i + 0.1));
            bins.add(bin);
        }
        petallength.setBins(bins);
        bins = new HashSet<>();
        bins.add(missing);
        for (double i = 0.1; i < 2.5; i += 0.1) {
            Bin bin = new Bin();
            bin.setLowerLimit(String.valueOf(i));
            bin.setUpperLimit(String.valueOf(i + 0.1));
            bins.add(bin);
        }
        petalwidth.setBins(bins);

        //list nodes in the order you want the attributes printed
        return Arrays.asList(sepallength, sepalwidth, petallength, petalwidth, label);
    }

    public static List<WebNode> buildAsiaNetwork() {
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

    public static WebNode createWebNode(String name, Attribute.AttributeType type, List<String> parents) {
        WebNode n = new WebNode();
        n.setType(type);
        n.setName(name);
        n.setParents(parents);
        return n;
    }

}