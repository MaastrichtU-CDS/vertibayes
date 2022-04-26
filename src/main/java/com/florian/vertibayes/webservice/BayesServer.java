package com.florian.vertibayes.webservice;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.data.Data;
import com.florian.nscalarproduct.station.DataStation;
import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirementsRequest;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.webservice.domain.NodesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.florian.nscalarproduct.data.Parser.parseCsv;

@RestController
public class BayesServer extends Server {
    public static final double BIN_UPPER_LIMIT_INCLUDE = 1.01;
    private Data data;
    private Map<String, Set<String>> uniqueValues = new HashMap<>();
    private static final int MINCOUNT = 10;
    private static final double MINPERCENTAGE = 0.1;


    public BigInteger count() {
        return Arrays.stream(localData).reduce(BigInteger::add).get();
    }

    @Value ("${datapath}")
    private String path;

    public BayesServer() {
        super();
    }

    public BayesServer(String id, List<ServerEndpoint> endpoints) {
        this.serverId = id;
        this.setEndpoints(endpoints);
    }

    public BayesServer(String path, String id) {
        this.path = path;
        this.serverId = id;
        readData();
    }

    private void readData() {
        if (System.getenv("DATABASE_URI") != null) {
            // Check if running in vantage6 by looking for system env, if yes change to database_uri system env for path
            this.path = System.getenv("DATABASE_URI");
        }
        this.data = parseCsv(path, 0);
        for (String name : data.getCollumnIds().keySet()) {
            uniqueValues.put(name, Data.getUniqueValues(data.getAttributeValues(name)));
        }
    }

    @GetMapping ("getUniqueValues")
    public Set<String> getUniqueValues(String attribute) {
        if (this.data == null) {
            readData();
        }
        return uniqueValues.get(attribute) == null ? new HashSet<>() : uniqueValues.get(attribute);
    }

    @GetMapping ("getBins")
    public Set<Bin> getBins(String attribute) {
        Set<Bin> bins = new HashSet<>();
        if (this.data == null) {
            readData();
        }
        if (uniqueValues.get(attribute) == null) {
            // attribute not locally known
            return bins;
        }
        Set<String> unique = uniqueValues.get(attribute) == null ? new HashSet<>() : uniqueValues.get(attribute);

        if (unique.contains("?")) {
            //create bin for unknown values
            bins.add(createBin("?", "?"));
            unique.remove("?");
        }

        bins.addAll(createBins(unique, attribute));
        return bins;

    }

    private Set<Bin> createBins(Set<String> unique, String attribute) {
        Bin currentBin = new Bin();
        Bin lastBin = currentBin;
        Set<Bin> bins = new HashSet<>();
        Attribute.AttributeType type = data.getAttributeType(attribute);
        if (unique.size() == 1) {
            //only one unique value
            String value = findSmallest(unique.stream().collect(Collectors.toList()), type);
            currentBin.setUpperLimit(value);
            currentBin.setLowerLimit(value);
        } else {
            //set lowest lower limit
            String lower = findSmallest(unique.stream().collect(Collectors.toList()), type);
            unique.remove(lower);
            currentBin.setLowerLimit(lower);
            while (unique.size() > 0) {
                boolean lastBinToosmall = false;
                //create bins
                while (!binIsBigEnough(currentBin, attribute)) {
                    if (unique.size() == 0) {
                        //ran out of possible candidates for upperLimits before reaching a large enough bin
                        lastBinToosmall = true;
                        break;
                    }
                    //look for new upperlimit
                    String upper = findSmallest(unique.stream().collect(Collectors.toList()), type);
                    unique.remove(upper);
                    currentBin.setUpperLimit(upper);
                }
                if (!lastBinToosmall) {
                    //found a upperLimit that makes the bin big enough
                    bins.add(currentBin);
                    lastBin = currentBin;
                    currentBin = new Bin();
                    //set lower limit to the previous upperLimit
                    currentBin.setLowerLimit(lastBin.getUpperLimit());
                } else {
                    //did not find a large enough bin
                    //find last upper limit
                    String lastUpper = "";
                    if (currentBin.getUpperLimit() != null) {
                        lastUpper = currentBin.getUpperLimit();
                    } else {
                        lastUpper = lastBin.getUpperLimit();
                    }
                    //increase the last upper Limit slightly so it is actually included
                    if (data.getAttributeType(attribute) == Attribute.AttributeType.numeric) {
                        //attribute is an integer
                        lastUpper = String.valueOf(Integer.parseInt(lastUpper) + 1);
                    } else {
                        //attribute is a double
                        lastUpper = String.valueOf(Double.parseDouble(lastUpper) * BIN_UPPER_LIMIT_INCLUDE);
                    }
                    lastBin.setUpperLimit(lastUpper);
                    if (bins.size() == 0) {
                        //if the first bin is already too small, make sure to add it at this point
                        bins.add(lastBin);
                    }
                }

            }
        }
        return bins;
    }

    private boolean binIsBigEnough(Bin currentBin, String attribute) {
        if (currentBin.getUpperLimit() == null || currentBin.getUpperLimit().length() == 0) {
            //bin has no upper limit yet
            return false;
        } else {
            Attribute lower = new Attribute(data.getAttributeType(attribute), currentBin.getLowerLimit(), attribute);
            Attribute upper = new Attribute(data.getAttributeType(attribute), currentBin.getUpperLimit(), attribute);

            AttributeRequirement req = new AttributeRequirement(lower, upper);

            //count the # of individuals that fall within this bin
            double count = 0;
            for (Attribute value : data.getAttributeValues(attribute)) {
                if (req.checkRequirement(value)) {
                    count++;
                }
            }
            return count >= MINCOUNT && count / ((double) data.getNumberOfIndividuals()) >= MINPERCENTAGE;
        }
    }

    private String findSmallest(List<String> unique, Attribute.AttributeType type) {
        double smallest = Double.parseDouble(unique.get(0));
        for (int i = 1; i < unique.size(); i++) {
            double temp = Double.parseDouble(unique.get(i));
            if (temp < smallest) {
                smallest = temp;
            }
        }
        if (type == Attribute.AttributeType.numeric) {
            //manually turn into an int, otherwise java returns adds a .0
            return String.valueOf(((int) smallest));
        }
        return String.valueOf(smallest);
    }


    private Bin createBin(String lower, String upper) {
        Bin bin = new Bin();
        bin.setLowerLimit(lower);
        bin.setUpperLimit(upper);
        return bin;
    }


    @PutMapping ("initK2Data")
    public void initK2Data(@RequestBody AttributeRequirementsRequest request) {
        reset();
        readData();
        List<AttributeRequirement> requirements = request.getRequirements() == null ? new ArrayList<>()
                : request.getRequirements();
        int population = data.getNumberOfIndividuals();
        localData = new BigInteger[population];
        for (int i = 0; i < population; i++) {
            localData[i] = BigInteger.ONE;
        }

        List<List<Attribute>> values = data.getData();
        for (AttributeRequirement req : requirements) {
            if (data.getAttributeCollumn(req.getName()) == null) {
                // attribute not locally available, skip
                continue;
            }
            for (int i = 0; i < population; i++) {
                if (!req.checkRequirement(values.get(data.getAttributeCollumn(req.getName())).get(i))) {
                    localData[i] = BigInteger.ZERO;
                }
            }
        }
        this.population = localData.length;
        this.dataStations.put("start", new DataStation(this.serverId, this.localData));
    }

    @GetMapping ("createNodes")
    public NodesResponse createNodes() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < data.getData().size(); i++) {
            if (i == data.getIdColumn()) {
                continue;
            }
            Attribute attribute = data.getData().get(i).get(0);
            nodes.add(new Node(attribute.getAttributeName(), uniqueValues.get(attribute.getAttributeName()),
                               attribute.getType()));
        }
        NodesResponse response = new NodesResponse();
        response.setNodes(nodes);
        return response;
    }


    @Override
    protected void reset() {
        dataStations = new HashMap<>();
        secretStations = new HashMap<>();
    }

}
