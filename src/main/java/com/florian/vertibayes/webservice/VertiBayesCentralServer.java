package com.florian.vertibayes.webservice;

import com.florian.nscalarproduct.station.CentralStation;
import com.florian.nscalarproduct.webservice.CentralServer;
import com.florian.nscalarproduct.webservice.Protocol;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Network;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.bayes.ParentValue;
import com.florian.vertibayes.bayes.Theta;
import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.webservice.domain.AttributeRequirementsRequest;
import com.florian.vertibayes.webservice.domain.InitCentralServerRequest;
import com.florian.vertibayes.webservice.domain.MaximumLikelyhoodRequest;
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

@RestController
public class VertiBayesCentralServer extends CentralServer {
    public static final double ONE = 0.99;
    //inherets endpoints from centralserver
    //overriding endpoints is impossible, use a different endpoint if you want to override

    private Network network;
    private List<ServerEndpoint> endpoints = new ArrayList<>();
    private ServerEndpoint secretEndpoint;
    private boolean isCentral = false;

    private static final double MINIMUM_LIKELYHOOD = 0.001;

    public VertiBayesCentralServer() {

    }

    @GetMapping ("buildNetwork")
    public List<Node> buildNetwork() {
        initEndpoints();
        endpoints.stream().forEach(x -> ((VertiBayesEndpoint) x).initK2Data(new ArrayList<>()));
        endpoints.stream().forEach(x -> x.initEndpoints());
        network = new Network(endpoints, secretEndpoint, this);
        network.createNetwork();
        return network.getNodes();
    }


    @PostMapping ("maximumLikelyhood")
    public List<Node> maximumLikelyhood(@RequestBody MaximumLikelyhoodRequest req) {
        initEndpoints();
        endpoints.stream().forEach(x -> x.initEndpoints());
        initThetas(req.getNodes());
        return req.getNodes();
    }

    @PostMapping ("initCentralServer")
    public void initCentralServer(@RequestBody InitCentralServerRequest req) {
        //purely exists for vantage6
        this.secretServer = req.getSecretServer();
        this.servers = req.getServers();
    }

    public BigInteger nparty(List<ServerEndpoint> endpoints, ServerEndpoint secretServer) {
        CentralStation station = new CentralStation();
        Protocol prot = new Protocol(endpoints, secretServer, "start");
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
    }

    public void initEndpoints(List<ServerEndpoint> endpoints, ServerEndpoint secretServer) {
        //this only exists for testing purposes
        this.endpoints = endpoints;
        this.secretEndpoint = secretServer;
    }

    private void initNode(Node node) {
        for (ServerEndpoint endpoint : endpoints) {
            node.getUniquevalues().addAll(((VertiBayesEndpoint) endpoint).getUniqueValues(node));
        }
    }

    private void initThetas(List<Node> nodes) {
        nodes.stream().forEach(x -> initNode(x));
        for (Node node : nodes) {
            for (String unique : node.getUniquevalues()) {
                // generate base thetas
                Theta t = new Theta();
                t.setLocalValue(new Attribute(node.getType(), unique, node.getName()));
                node.getProbabilities().add(t);
            }
            for (Node parent : node.getParents()) {
                // for each parent
                List<Theta> copies = new ArrayList<>();

                for (String p : parent.getUniquevalues()) {
                    // for each parent value
                    ParentValue v = new ParentValue();
                    v.setName(parent.getName());
                    v.setValue(new Attribute(parent.getType(), p, parent.getName()));
                    for (Theta t : node.getProbabilities()) {
                        //Copy each current child, add the extra new parent
                        Theta copy = new Theta();
                        copy.setLocalValue(t.getLocalValue());

                        copy.setParents(new ArrayList<>());
                        copy.getParents().addAll(t.getParents());
                        copy.getParents().add(v);
                        copies.add(copy);
                    }
                }
                // remove old children, put in the new copies
                node.getProbabilities().removeAll(node.getProbabilities());
                node.getProbabilities().addAll(copies);
            }
            for (Theta t : node.getProbabilities()) {
                determineProb(t);
            }
            alignSliblings(node);
        }
    }

    private void determineProb(Theta t) {
        AttributeRequirementsRequest r = new AttributeRequirementsRequest();
        r.setRequirements(new ArrayList<>());

        if (t.getParents().size() > 0) {
            List<Attribute> parents = t.getParents().stream().map(x -> x.getValue()).collect(Collectors.toList());
            List<Attribute> list = new ArrayList<>();
            list.addAll(parents);
            list.add(t.getLocalValue());
            BigInteger count = countValue(list);
            BigInteger parentCount = countValue(parents);
            t.setP(count.doubleValue() / parentCount.doubleValue());
        } else {
            BigInteger count = countValue(Arrays.asList(t.getLocalValue()));
            t.setP(count.doubleValue() / (double) endpoints.get(0).getPopulation());
        }
    }

    private BigInteger countValue(List<Attribute> attributes) {
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
