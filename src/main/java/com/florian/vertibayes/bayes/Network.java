package com.florian.vertibayes.bayes;


import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.vertibayes.webservice.VertiBayesCentralServer;
import com.florian.vertibayes.webservice.VertiBayesEndpoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.florian.vertibayes.util.MathUtil.factorial;


public class Network {
    private List<Node> nodes = new ArrayList<>();
    private VertiBayesCentralServer central;
    private static final int MAX_PARENTS = 3;
    private final int rounding;
    private List<ServerEndpoint> endpoints;
    private ServerEndpoint secretServer;

    private static final int TEN = 10;
    private static final int HUNDRED = 100;

    public Network(List<ServerEndpoint> endpoints, ServerEndpoint secretServer, VertiBayesCentralServer central,
                   int population) {
        for (ServerEndpoint endpoint : endpoints) {
            nodes.addAll(((VertiBayesEndpoint) endpoint).createNode());
        }

        int r = HUNDRED;
        if (population > HUNDRED * TEN) {
            while (r < population) {
                // set ROUNDING decimals based on populationsize, with a minimum of 100 decimals because
                //K2 involves calculations like 1/(POPULATION!) which needs more decimals with a larger population
                // Only start doing this for populations > 1000
                r *= TEN;
            }
            r *= TEN;
        }
        rounding = r;

        removeDoubles();
        this.endpoints = endpoints;
        this.secretServer = secretServer;
        this.central = central;
    }

    public void createNetwork() {
        List<Node> pred = new ArrayList<>();
        for (Node node : nodes) {
            determineParent(node, pred);
            pred.add(node);
        }
    }


    private void determineParent(Node node, List<Node> pred) {
        List<Node> possibleParents = new ArrayList<>(pred);
        BigDecimal ri = BigDecimal.valueOf(node.getUniquevalues().size());
        BigDecimal ri1factorial = factorial(ri.subtract(BigDecimal.ONE));
        List<Node> parents = new ArrayList<>(node.getParents());
        List<List<AttributeRequirement>> requirements = determineRequirements(parents);
        BigDecimal curValue = calculateK2(node, ri, ri1factorial, requirements);

        List<Node> bestParents = new ArrayList<>();
        if (node.getParents().size() >= MAX_PARENTS) {
            return;
        }
        for (Node possibility : possibleParents) {
            List<Node> newParents = new ArrayList<>(node.getParents());

            newParents.add(possibility);
            requirements = determineRequirements(newParents);
            BigDecimal res = calculateK2(node, ri, ri1factorial, requirements);
            if (res.compareTo(curValue) > 0) {
                curValue = res;
                bestParents = newParents;
            }
        }
        node.setParents(bestParents);
    }

    private BigDecimal calculateK2(Node node, BigDecimal ri, BigDecimal ri1factorial,
                                   List<List<AttributeRequirement>> requirements) {
        BigDecimal sumaijk = BigDecimal.ZERO;
        BigDecimal prodaijk = BigDecimal.ONE;
        BigDecimal partial = BigDecimal.ONE;

        if (requirements.size() == 0) {
            if (node.getType() != Attribute.AttributeType.real) {
                for (String value : node.getUniquevalues()) {
                    List<AttributeRequirement> req = new ArrayList<>();
                    req.add(new AttributeRequirement(new Attribute(node.getType(), value, node.getName())));
                    //this should be a webservice call
                    BigDecimal aijk = calculateAijk(req);

                    sumaijk = sumaijk.add(aijk);
                    prodaijk = prodaijk.multiply(factorial(aijk));
                }
            } else {
                for (Bin bin : node.getBins()) {
                    List<AttributeRequirement> req = new ArrayList<>();
                    Attribute lowerLimit = new Attribute(node.getType(), bin.getLowerLimit(), node.getName());
                    Attribute upperLimit = new Attribute(node.getType(), bin.getUpperLimit(), node.getName());
                    req.add(new AttributeRequirement(lowerLimit, upperLimit));
                    //this should be a webservice call
                    BigDecimal aijk = calculateAijk(req);

                    sumaijk = sumaijk.add(aijk);
                    prodaijk = prodaijk.multiply(factorial(aijk));
                }
            }
            return (ri1factorial.divide(factorial(sumaijk.add(ri).subtract(BigDecimal.ONE)), rounding,
                                        RoundingMode.HALF_UP))
                    .multiply(prodaijk);
        } else {
            for (List<AttributeRequirement> req : requirements) {
                sumaijk = BigDecimal.ZERO;
                prodaijk = BigDecimal.ONE;
                if (node.getType() != Attribute.AttributeType.real) {
                    for (String value : node.getUniquevalues()) {
                        List<AttributeRequirement> r = new ArrayList<>(req);
                        r.add(new AttributeRequirement(new Attribute(node.getType(), value, node.getName())));

                        BigDecimal aijk = calculateAijk(r);
                        sumaijk = sumaijk.add(aijk);
                        prodaijk = prodaijk.multiply(factorial(aijk));
                    }
                } else {
                    for (Bin bin : node.getBins()) {
                        List<AttributeRequirement> r = new ArrayList<>(req);
                        Attribute lowerLimit = new Attribute(node.getType(), bin.getLowerLimit(), node.getName());
                        Attribute upperLimit = new Attribute(node.getType(), bin.getUpperLimit(), node.getName());
                        r.add(new AttributeRequirement(lowerLimit, upperLimit));
                        //this should be a webservice call
                        BigDecimal aijk = calculateAijk(r);

                        sumaijk = sumaijk.add(aijk);
                        prodaijk = prodaijk.multiply(factorial(aijk));
                    }
                }
                partial = partial.multiply(
                        (ri1factorial.divide(factorial(sumaijk.add(ri).subtract(BigDecimal.ONE)), rounding,
                                             RoundingMode.HALF_UP))
                                .multiply(prodaijk));
            }
        }
        return partial;
    }

    private BigDecimal calculateAijk(List<AttributeRequirement> req) {
        List<ServerEndpoint> relevantEndpoints = new ArrayList<>();
        endpoints.stream().forEach(x -> {
            if (((VertiBayesEndpoint) x).initK2Data(req).isRelevant()) {
                relevantEndpoints.add(x);
            }
        });
        if (relevantEndpoints.size() == 1) {
            return new BigDecimal(((VertiBayesEndpoint) relevantEndpoints.get(0)).getCount());
        } else {
            secretServer.addSecretStation("start", relevantEndpoints.stream().map(x -> x.getServerId()).collect(
                    Collectors.toList()), relevantEndpoints.get(0).getPopulation());
            return new BigDecimal(central.nparty(relevantEndpoints, secretServer));
        }
    }

    public List<List<AttributeRequirement>> determineRequirements(List<Node> nodes) {
        List<List<AttributeRequirement>> requirements = new ArrayList<>();
        for (Node node : nodes) {
            List<List<AttributeRequirement>> temp = new ArrayList<>();
            if (node.getType() != Attribute.AttributeType.real) {
                for (String value : node.getUniquevalues()) {
                    if (requirements.size() == 0) {
                        List<AttributeRequirement> req = new ArrayList<>();
                        req.add(new AttributeRequirement(new Attribute(node.getType(), value, node.getName())));
                        temp.add(req);
                    } else {
                        for (List<AttributeRequirement> requirement : requirements) {
                            List<AttributeRequirement> req = new ArrayList<>(requirement);
                            req.add(new AttributeRequirement(new Attribute(node.getType(), value, node.getName())));
                            temp.add(req);
                        }
                    }
                }
                requirements = temp;
            } else {
                for (Bin bin : node.getBins()) {
                    if (requirements.size() == 0) {
                        List<AttributeRequirement> req = new ArrayList<>();
                        Attribute lowerLimit = new Attribute(node.getType(), bin.getLowerLimit(), node.getName());
                        Attribute upperLimit = new Attribute(node.getType(), bin.getUpperLimit(), node.getName());
                        req.add(new AttributeRequirement(lowerLimit, upperLimit));
                        temp.add(req);
                    } else {
                        for (List<AttributeRequirement> requirement : requirements) {
                            List<AttributeRequirement> req = new ArrayList<>(requirement);
                            Attribute lowerLimit = new Attribute(node.getType(), bin.getLowerLimit(), node.getName());
                            Attribute upperLimit = new Attribute(node.getType(), bin.getUpperLimit(), node.getName());
                            req.add(new AttributeRequirement(lowerLimit, upperLimit));
                            temp.add(req);
                        }
                    }
                }
            }

        }
        return requirements;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    private void removeDoubles() {
        List<Node> copied = new ArrayList<>();
        for (Node node : nodes) {
            boolean contained = false;
            for (Node copy : copied) {
                if (copy.getName().equals(node.getName())) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                copied.add(node);
            }
        }
        nodes = copied;
    }


}
