package com.florian.vertibayes.webservice;

import com.florian.nscalarproduct.station.CentralStation;
import com.florian.nscalarproduct.webservice.CentralServer;
import com.florian.nscalarproduct.webservice.Protocol;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.vertibayes.bayes.Network;
import com.florian.vertibayes.bayes.Node;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RestController
public class VertiBayesCentralServer extends CentralServer {
    //inherets endpoints from centralserver
    //overriding endpoints is impossible, use a different endpoint if you want to override

    private Network network;

    public VertiBayesCentralServer() {

    }

    @GetMapping ("buildNetwork")
    public List<Node> buildNetwork() {
        List<ServerEndpoint> endpoints = new ArrayList<>();
        for (String s : servers) {
            endpoints.add(new VertiBayesEndpoint(s));
        }
        endpoints.stream().forEach(x -> ((VertiBayesEndpoint) x).initK2Data(new ArrayList<>()));
        endpoints.stream().forEach(x -> x.initEndpoints());
        ServerEndpoint secretEndpoint = new ServerEndpoint(secretServer);
        network = new Network(endpoints, secretEndpoint, this);
        network.createNetwork();
        return network.getNodes();
    }

    public BigInteger nparty(List<ServerEndpoint> endpoints, ServerEndpoint secretServer) {
        CentralStation station = new CentralStation();
        Protocol prot = new Protocol(endpoints, secretServer, "start");
        return station.calculateNPartyScalarProduct(prot);
    }

}
