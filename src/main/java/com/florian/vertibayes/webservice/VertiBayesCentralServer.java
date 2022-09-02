package com.florian.vertibayes.webservice;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.station.CentralStation;
import com.florian.nscalarproduct.webservice.CentralServer;
import com.florian.nscalarproduct.webservice.Protocol;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.bayes.*;
import com.florian.vertibayes.webservice.domain.InitCentralServerRequest;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationResponse;
import com.florian.vertibayes.webservice.domain.external.ExpectationMaximizationTestResponse;
import com.florian.vertibayes.webservice.domain.external.WebBayesNetwork;
import com.florian.vertibayes.webservice.mapping.WebNodeMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.florian.vertibayes.bayes.Node.findSliblings;
import static com.florian.vertibayes.webservice.mapping.WebNodeMapper.mapWebNodeFromNode;
import static com.florian.vertibayes.weka.WEKAExpectationMaximiation.wekaExpectationMaximization;

@RestController
public class VertiBayesCentralServer extends CentralServer {
    public static final double ONE = 0.99;
    public static final int PRECISION = 0; // Precision isn't relevant, as bayesian network calculations concern
    // integers as its counts of individuals for K2 and maximumlikelyhood etc.
    // inherets endpoints from centralserver
    //overriding endpoints is impossible, use a different endpoint if you want to override

    private Network network;
    private List<ServerEndpoint> endpoints = new ArrayList<>();
    private ServerEndpoint secretEndpoint;
    private boolean testing = false;
    private boolean isCentral = false;

    private static final double MINIMUM_LIKELYHOOD = 0.001;

    public VertiBayesCentralServer() {

    }

    public VertiBayesCentralServer(boolean testing) {
        this.testing = testing;
    }

    @GetMapping ("buildNetwork")
    public WebBayesNetwork buildNetwork() {
        initEndpoints();
        endpoints.stream().forEach(x -> ((VertiBayesEndpoint) x).initK2Data(new ArrayList<>()));
        network = new Network(endpoints, secretEndpoint, this);
        network.createNetwork();
        WebBayesNetwork response = new WebBayesNetwork();
        response.setNodes(mapWebNodeFromNode(network.getNodes()));
        return response;
    }


    @PostMapping ("maximumLikelyhood")
    public WebBayesNetwork maximumLikelyhood(@RequestBody WebBayesNetwork req) {
        initEndpoints();
        List<Node> nodes = WebNodeMapper.mapWebNodeToNode(req.getNodes());
        initNodesMaximumLikelyhood(nodes, req.getMinPercentage());
        initThetas(nodes);
        WebBayesNetwork response = new WebBayesNetwork();
        response.setNodes(mapWebNodeFromNode(nodes));
        return response;
    }

    @PostMapping ("ExpectationMaximization")
    public ExpectationMaximizationResponse expectationMaximization(@RequestBody WebBayesNetwork req) throws Exception {
        initEndpoints();
        List<Node> nodes = WebNodeMapper.mapWebNodeToNode(req.getNodes());
        initNodesMaximumLikelyhood(nodes, req.getMinPercentage());
        initThetas(nodes);

        ExpectationMaximizationTestResponse res = wekaExpectationMaximization(mapWebNodeFromNode(nodes),
                                                                              req.getTarget());
        if (!testing) {
            ExpectationMaximizationResponse response = new ExpectationMaximizationResponse();
            response.setNodes(res.getNodes());
            response.setSyntheticTrainingAuc(res.getSyntheticAuc());
            return response;
        }
        return res;
    }

    @PostMapping ("initCentralServer")
    public void initCentralServer(@RequestBody InitCentralServerRequest req) {
        //purely exists for vantage6
        super.secretServer = req.getSecretServer();
        super.servers = req.getServers();
    }

    public BigInteger nparty(List<ServerEndpoint> endpoints, ServerEndpoint secretServer) {
        CentralStation station = new CentralStation();
        Protocol prot = new Protocol(endpoints, secretServer, "start", PRECISION);
        return station.calculateNPartyScalarProduct(prot);
    }

    private void initEndpoints() {
        if (endpoints.size() == 0) {
            endpoints = new ArrayList<>();
            for (String s : servers) {
                endpoints.add(new VertiBayesEndpoint(s));
            }
        }
        if (secretEndpoint == null) {
            secretEndpoint = new ServerEndpoint(secretServer);
        }
        endpoints.stream().forEach(x -> x.initEndpoints());
        secretEndpoint.initEndpoints();
    }

    public void initEndpoints(List<ServerEndpoint> endpoints, ServerEndpoint secretServer) {
        //this only exists for testing purposes
        this.endpoints = endpoints;
        this.secretEndpoint = secretServer;
    }

    private void initNodesMaximumLikelyhood(List<Node> nodes, double minPercentage) {
        for (Node n : nodes) {
            if (n.getType() != Attribute.AttributeType.real && n.getType() != Attribute.AttributeType.numeric) {
                initNode(n);
            } else if (n.getBins().isEmpty()) {
                initNodeBinned(n, minPercentage);
            }
        }
    }

    private void initNode(Node node) {
        for (ServerEndpoint endpoint : endpoints) {
            node.getUniquevalues().addAll(((VertiBayesEndpoint) endpoint).getUniqueValues(node.getName()));
        }
    }

    private void initNodeBinned(Node node, double minPercentage) {
        for (ServerEndpoint endpoint : endpoints) {
            node.getBins().addAll(((VertiBayesEndpoint) endpoint).getBins(node.getName(), minPercentage));
        }
    }

    private void initThetas(List<Node> nodes) {
        for (Node node : nodes) {
            if (node.getType() != Attribute.AttributeType.real && node.getType() != Attribute.AttributeType.numeric) {
                for (String unique : node.getUniquevalues()) {
                    // generate base thetas
                    Theta t = new Theta();
                    t.setLocalRequirement(
                            new AttributeRequirement(new Attribute(node.getType(), unique, node.getName())));
                    node.getProbabilities().add(t);
                }
            } else {
                for (Bin bin : node.getBins()) {
                    Theta t = new Theta();
                    Attribute lowerLimit = new Attribute(node.getType(), bin.getLowerLimit(), node.getName());
                    Attribute upperLimit = new Attribute(node.getType(), bin.getUpperLimit(), node.getName());
                    t.setLocalRequirement(new AttributeRequirement(lowerLimit, upperLimit));
                    node.getProbabilities().add(t);
                }
            }
            for (Node parent : node.getParents()) {
                // for each parent
                List<Theta> copies = new ArrayList<>();
                if (parent.getType() != Attribute.AttributeType.real &&
                        parent.getType() != Attribute.AttributeType.numeric) {
                    for (String p : parent.getUniquevalues()) {
                        // for each parent value
                        ParentValue v = new ParentValue();
                        v.setName(parent.getName());
                        v.setRequirement(
                                new AttributeRequirement(new Attribute(parent.getType(), p, parent.getName())));
                        for (Theta t : node.getProbabilities()) {
                            //Copy each current child, add the extra new parent
                            Theta copy = new Theta();
                            copy.setLocalRequirement(t.getLocalRequirement());
                            copy.setParents(new ArrayList<>());
                            copy.getParents().addAll(t.getParents());
                            copy.getParents().add(v);
                            copies.add(copy);
                        }
                    }
                } else {
                    for (Bin bin : parent.getBins()) {
                        // for each parent value
                        ParentValue v = new ParentValue();
                        v.setName(parent.getName());
                        Attribute lowerLimit = new Attribute(node.getType(), bin.getLowerLimit(), parent.getName());
                        Attribute upperLimit = new Attribute(node.getType(), bin.getUpperLimit(), parent.getName());
                        v.setRequirement(
                                new AttributeRequirement(lowerLimit, upperLimit));
                        for (Theta t : node.getProbabilities()) {
                            //Copy each current child, add the extra new parent
                            Theta copy = new Theta();
                            copy.setLocalRequirement(t.getLocalRequirement());
                            copy.setParents(new ArrayList<>());
                            copy.getParents().addAll(t.getParents());
                            copy.getParents().add(v);
                            copies.add(copy);
                        }
                    }
                }
                // remove old children, put in the new copies
                node.getProbabilities().removeAll(node.getProbabilities());
                node.getProbabilities().addAll(copies);
            }
            for (Theta t : node.getProbabilities()) {
                determineProb(t, node);
                //set calculated to true
                t.calculated();
            }
            alignSliblings(node);
        }
    }

    private void determineProb(Theta t, Node n) {

        if (t.getParents().size() > 0) {
            List<Theta> sliblings = findSliblings(t, n);
            //Check if all sliblings already have an assigned value
            if (sliblingsHaveTheta(sliblings)) {
                //if all sliblings have a P value, then insert count into P value, then calculate
                //proper P by calculating P / TOTAL
                t.setP(getCount(t).doubleValue());

                calculateP(t, sliblings);
            } else {
                //if sliblings do not have a P yet, then simply insert count into P value
                t.setP(getCount(t).doubleValue());
            }
        } else {
            List<Theta> sliblings = findSliblings(t, n);
            //Check if all sliblings already have an assigned value, if so this theta will just be 1-p
            //Otherwise calculate theta properly
            if (sliblingsHaveTheta(sliblings)) {
                calculateOneMinusSliblings(t, sliblings);
            } else {
                BigInteger count = countValue(
                        new ArrayList<>(Arrays.asList(t.getLocalRequirement())));
                t.setP(count.doubleValue() / (double) endpoints.get(0).getPopulation());
            }
        }
    }

    private BigInteger getCount(Theta t) {
        List<AttributeRequirement> listReq = t.getParents().parallelStream().map(x -> x.getRequirement())
                .collect(Collectors.toList());
        listReq.add(t.getLocalRequirement());
        return countValue(listReq);
    }

    private void calculateOneMinusSliblings(Theta t, List<Theta> sliblings) {
        double p = 0;
        for (Theta slibling : sliblings) {
            //add 0 when encountering NaN
            p += Double.isNaN(slibling.getP()) ? 0 : slibling.getP();
        }
        t.setP(1 - p);
    }

    private void calculateP(Theta t, List<Theta> sliblings) {
        // calculates P under the assumption sliblings currently contain counts.
        // So P = slibling.getP() / sum(sliblings.getP())
        // This way total count does not need to be calculated seperatly
        double total = 0;
        for (Theta slibling : sliblings) {
            //add 0 when encountering NaN
            total += Double.isNaN(slibling.getP()) ? 0 : slibling.getP();
        }
        for (Theta slibling : sliblings) {
            slibling.setP(slibling.getP() / total);
        }
    }

    private boolean sliblingsHaveTheta(List<Theta> sliblings) {
        int count = 0;
        for (Theta t : sliblings) {
            if (!t.isCalculated()) {
                count++;
            }
            if (count > 1) {
                return false;
            }
        }
        return true;
    }

    private BigInteger countValue(List<AttributeRequirement> attributes) {
        for (ServerEndpoint endpoint : endpoints) {
            ((VertiBayesEndpoint) endpoint).initK2Data(attributes);
        }

        secretEndpoint.addSecretStation("start", endpoints.stream().map(x -> x.getServerId()).collect(
                Collectors.toList()), endpoints.get(0).getPopulation());
        return nparty(endpoints, secretEndpoint);
    }

    private void alignSliblings(Node node) {
        //make sure no thetas are set to zero as this breaks bayesian networks
        //Also make sure all sliblings add up to one, due to certain combinations not existing in the test set
        //You can see weird behaviour there
        boolean done = false;
        while (!done) {
            done = true;
            for (Theta t : node.getProbabilities()) {
                List<Theta> sliblings = findSliblings(t, node);
                double sum = 0;
                for (Theta x : sliblings) {
                    sum += x.getP();
                }
                if (sum < ONE || Double.isNaN(sum)) {
                    //this set of sliblings does not occur in the training set and only theoretically exists
                    //So just set everything randomly
                    for (Theta x : sliblings) {
                        x.setP(1.0 / sliblings.size());
                    }
                }

                if (t.getP() <= 0 || Double.isNaN(t.getP())) {
                    // parent-child value does not occur in the training set, initiallize as minimum likelyhood
                    // alter slibling values accordingly
                    done = false;
                    for (Theta s : sliblings) {
                        s.setP(s.getP() - MINIMUM_LIKELYHOOD / (sliblings.size() - 1));
                    }
                    t.setP(MINIMUM_LIKELYHOOD);
                }
            }
        }
    }
}
