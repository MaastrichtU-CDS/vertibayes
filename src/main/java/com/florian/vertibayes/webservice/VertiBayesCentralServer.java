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

@RestController
public class VertiBayesCentralServer extends CentralServer {
    //inherets endpoints from centralserver
    //overriding endpoints is impossible, use a different endpoint if you want to override

    private Network network;
    private List<ServerEndpoint> endpoints = new ArrayList<>();
    private ServerEndpoint secretEndpoint;

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
        initThetas(req.getNodes(), endpoints);
        return req.getNodes();
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

    private void initThetas(List<Node> nodes, List<ServerEndpoint> endpoints) {
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
}
