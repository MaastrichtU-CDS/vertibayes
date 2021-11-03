package florian.bayes;


import florian.bayes.data.Attribute;
import florian.bayes.stations.DataOwner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static florian.util.Util.factorial;


public class Network {
    private List<Node> nodes = new ArrayList<>();
    private List<DataOwner> dataOwners;
    private static final int MAX_PARENTS = 3;
    private static final int ROUNDING = 100;

    public Network(List<DataOwner> dataOwners) {
        this.dataOwners = dataOwners;
        for (DataOwner owner : dataOwners) {
            nodes.addAll(owner.createNodes());
        }
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
        List<List<Attribute>> requirements = determineRequirements(parents);
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
                                   List<List<Attribute>> requirements) {
        BigDecimal sumaijk = BigDecimal.ZERO;
        BigDecimal prodaijk = BigDecimal.ONE;
        BigDecimal partial = BigDecimal.ONE;

        if (requirements.size() == 0) {
            for (String value : node.getUniquevalues()) {
                List<Attribute> req = new ArrayList<>();
                req.add(new Attribute(node.getType(), value, node.getName()));

                //this should be a webservice call
//                List<DataStation> stations = dataOwners.parallelStream().map(x -> {
//                    x.createStation(req);
//                    return x.getStation();
//                }).collect(
//                        Collectors.toList());
//                BigDecimal aijk = new BigDecimal(central.calculateNPartyScalarProduct(stations));

                BigDecimal aijk = BigDecimal.ONE;
                sumaijk = sumaijk.add(aijk);
                prodaijk = prodaijk.multiply(factorial(aijk));
            }
            return (ri1factorial.divide(factorial(sumaijk.add(ri).subtract(BigDecimal.ONE)), ROUNDING,
                                        RoundingMode.HALF_UP))
                    .multiply(prodaijk);
        } else {
            for (List<Attribute> req : requirements) {
                sumaijk = BigDecimal.ZERO;
                prodaijk = BigDecimal.ONE;
                for (String value : node.getUniquevalues()) {
                    List<Attribute> r = new ArrayList<>(req);
                    r.add(new Attribute(node.getType(), value, node.getName()));

//                    //this should be a webservice call
//                    List<DataStation> stations = dataOwners.parallelStream().map(x -> {
//                        x.createStation(r);
//                        return x.getStation();
//                    }).collect(
//                            Collectors.toList());
//                    BigDecimal aijk = new BigDecimal(central.calculateNPartyScalarProduct(stations));
                    BigDecimal aijk = BigDecimal.ONE;
                    sumaijk = sumaijk.add(aijk);
                    prodaijk = prodaijk.multiply(factorial(aijk));
                }
                partial = partial.multiply(
                        (ri1factorial.divide(factorial(sumaijk.add(ri).subtract(BigDecimal.ONE)), ROUNDING,
                                             RoundingMode.HALF_UP))
                                .multiply(prodaijk));
            }
        }
        return partial;
    }

    public List<List<Attribute>> determineRequirements(List<Node> nodes) {
        List<List<Attribute>> requirements = new ArrayList<>();
        for (Node node : nodes) {
            List<List<Attribute>> temp = new ArrayList<>();
            for (String value : node.getUniquevalues()) {
                if (requirements.size() == 0) {
                    List<Attribute> req = new ArrayList<>();
                    req.add(new Attribute(node.getType(), value, node.getName()));
                    temp.add(req);
                } else {
                    for (List<Attribute> requirement : requirements) {
                        List<Attribute> req = new ArrayList<>(requirement);
                        req.add(new Attribute(node.getType(), value, node.getName()));
                        temp.add(req);
                    }
                }
            }
            requirements = temp;
        }
        return requirements;
    }

    public List<Node> getNodes() {
        return nodes;
    }


}
