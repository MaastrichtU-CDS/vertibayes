package com.florian.vertibayes.webservice;

import com.florian.nscalarproduct.station.CentralStation;
import com.florian.nscalarproduct.webservice.CentralServer;
import com.florian.nscalarproduct.webservice.Protocol;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

@RestController
public class VertiBayesCentralServer extends CentralServer {
    //inherets endpoints from centralserver
    //overriding endpoints is impossible, use a different endpoint if you want to override


    public BigInteger nparty(List<ServerEndpoint> endpoints, ServerEndpoint secretServer) {
//        for (String s : servers) {
//            endpoints.add(new ServerEndpoint(s));
//        }

        Integer population = endpoints.get(0).getPopulation();

        //REST_TEMPLATE.put(secretServer + "/initRandom", population);

        CentralStation station = new CentralStation();

        Protocol prot = new Protocol(endpoints, secretServer, "start");
        return station.calculateNPartyScalarProduct(prot);
    }

}
