package com.florian.vertibayes.notunittests.generatedata;

import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.data.Attribute;
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

    private static WebNode createWebNode(String name, Attribute.AttributeType type, List<String> parents) {
        WebNode n = new WebNode();
        n.setType(type);
        n.setName(name);
        n.setParents(parents);
        return n;
    }

}