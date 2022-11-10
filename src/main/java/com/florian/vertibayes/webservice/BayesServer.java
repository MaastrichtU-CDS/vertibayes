package com.florian.vertibayes.webservice;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.nscalarproduct.data.Data;
import com.florian.nscalarproduct.error.InvalidDataFormatException;
import com.florian.nscalarproduct.station.DataStation;
import com.florian.nscalarproduct.webservice.Server;
import com.florian.nscalarproduct.webservice.ServerEndpoint;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirement;
import com.florian.nscalarproduct.webservice.domain.AttributeRequirementsRequest;
import com.florian.vertibayes.bayes.Bin;
import com.florian.vertibayes.bayes.Node;
import com.florian.vertibayes.webservice.domain.ActiveRecordRequest;
import com.florian.vertibayes.webservice.domain.InitDataResponse;
import com.florian.vertibayes.webservice.domain.NodesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.florian.nscalarproduct.data.Parser.parseData;

@RestController
public class BayesServer extends Server {
    public static final double BIN_UPPER_LIMIT_INCLUDE = 1.01;
    private Data data;
    private Map<String, Set<String>> uniqueValues = new HashMap<>();
    private static final int MINCOUNT = 10;
    private static final double MINPERCENTAGE_DEFAULT = 0.1;
    private double minPercentage = MINPERCENTAGE_DEFAULT;
    private static final List<Double> VALID_PERCENTAGES = Arrays.asList(0.1, 0.2, 0.25, 0.3, 0.4);
    private boolean useLocalOnly = false; //flag to indicate if a hybrid distribution is to be assumed
    // if you only use local data records with "locallyPresent" = false will be set to 0 for n-party protocols
    // default = false, locallyPresent are treated as 1.

    private boolean[] activeRecords;


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

    protected void readData() {
        if (System.getenv("DATABASE_URI") != null) {
            // Check if running in vantage6 by looking for system env, if yes change to database_uri system env for path
            this.path = System.getenv("DATABASE_URI");
        }
        try {
            this.data = parseData(path, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidDataFormatException e) {
            e.printStackTrace();
        }
        for (String name : data.getCollumnIds().keySet()) {
            uniqueValues.put(name, Data.getUniqueValues(data.getAttributeValues(name)));
        }
    }

    @GetMapping ("setUseLocalOnly")
    public void setUseLocalOnly(boolean useLocalOnly) {
        this.useLocalOnly = useLocalOnly;
    }

    @GetMapping ("getLocalPopulation")
    public int getLocalPopulation() {
        if (useLocalOnly || useKfold()) {
            int count = 0;
            for (int i = 0; i < population; i++) {
                if (useKfold() && useLocalOnly) {
                    //both kfold and hybrid split
                    if (recordIsLocallyPresent(i) && recordIsActive(i)) {
                        count++;
                    }
                } else if (useLocalOnly && recordIsLocallyPresent(i)) {
                    //only hybrid split
                    count++;
                } else if (useKfold() && recordIsActive(i)) {
                    // only kfold
                    count++;
                }
            }
            return count;
        } else {
            return super.getPopulation();
        }
    }

    private boolean useKfold() {
        return activeRecords != null && activeRecords.length > 0;
    }

    @PostMapping ("setActiveRecords")
    public void setActiveRecords(@RequestBody ActiveRecordRequest req) {
        this.activeRecords = req.getActiveRecords();
    }

    @GetMapping ("getUniqueValues")
    public Set<String> getUniqueValues(String attribute) {
        if (this.data == null) {
            readData();
        }
        return uniqueValues.get(attribute) == null ? new HashSet<>() : uniqueValues.get(attribute);
    }

    @GetMapping ("getBins")
    public Set<Bin> getBins(String attribute, double minPercentage) {
        setMinPercentage(minPercentage);
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
            removeValue(unique, lower);
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
                    removeValue(unique, upper);
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

    private void removeValue(Set<String> unique, String value) {
        //remove both the double and int variant of this value
        //doubles are automaticly translated into x.0 by java
        //if the original data simply contained x it'll get stuck in an endless loop
        unique.remove(value);
        unique.remove(String.valueOf((int) Double.parseDouble(value)));

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
            return count >= MINCOUNT && count / ((double) data.getNumberOfIndividuals()) >= minPercentage;
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

    @GetMapping ("getCount")
    public BigInteger getCount() {
        BigInteger count = BigInteger.ZERO;
        for (int i = 0; i < localData.length; i++) {
            count = count.add(localData[i]);
        }
        return count;
    }


    @PostMapping ("initK2Data")
    public InitDataResponse initK2Data(@RequestBody AttributeRequirementsRequest request) {
        reset();
        readData();
        InitDataResponse response = new InitDataResponse();

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
            //if you get here local data contains one of the attributes, ergo this endpoint is relevant.
            response.setRelevant(true);
            for (int i = 0; i < population; i++) {
                if (values.get(data.getAttributeCollumn(req.getName())).get(i).isUnknown()) {
                    if (!req.checkRequirement(getMeanOrMode(req.getName()))) {
                        localData[i] = BigInteger.ZERO;
                    }
                }
                if (!req.checkRequirement(values.get(data.getAttributeCollumn(req.getName())).get(i))) {
                    localData[i] = BigInteger.ZERO;
                }
            }
        }

        if (!useLocalOnly) {
            checkHorizontalSplit(data, localData);
        } else {
            markLocallyPresent(data, localData);
        }
        markActiveRecords(localData);

        this.population = localData.length;
        this.dataStations.put("start", new DataStation(this.serverId, this.localData));

        return response;
    }

    private Attribute getMeanOrMode(String attributeName) {
        List<Attribute> attribute = data.getAttributeValues(attributeName);
        Attribute.AttributeType type = data.getAttributeType(attributeName);
        if (type == Attribute.AttributeType.string || type == Attribute.AttributeType.bool) {
            //nominal value so get mode
            Map<String, Integer> counts = new HashMap<>();
            for (Attribute a : attribute) {
                if (!a.getValue().equals("?")) {
                    if (counts.get(a.getValue()) == null) {
                        counts.put(a.getValue(), 1);
                    } else {
                        counts.put(a.getValue(), counts.get(a.getValue()) + 1);
                    }
                }
            }
            int max = 0;
            String modevalue = "";
            for (String key : counts.keySet()) {
                if (counts.get(key) > max) {
                    max = counts.get(key);
                    modevalue = key;
                }
            }
            return new Attribute(type, modevalue, attributeName);
        } else {
            //numeric value so get mean
            double sum = 0;
            int count = 0;
            for (Attribute a : attribute) {
                if (!a.getValue().equals("?")) {
                    sum += Double.parseDouble(a.getValue());
                    count++;
                }
            }
            sum /= count;
            if (type == Attribute.AttributeType.numeric) {
                return new Attribute(type, String.valueOf((int) sum), attributeName);
            } else {
                return new Attribute(type, String.valueOf(sum), attributeName);
            }
        }

    }

    @PostMapping ("initMaximumLikelyhoodData")
    public InitDataResponse initMaximumLikelyhoodData(@RequestBody AttributeRequirementsRequest request) {
        reset();
        readData();
        InitDataResponse response = new InitDataResponse();

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
            //if you get here local data contains one of the attributes, ergo this endpoint is relevant.
            response.setRelevant(true);
            for (int i = 0; i < population; i++) {
                if (!req.checkRequirement(values.get(data.getAttributeCollumn(req.getName())).get(i))) {
                    localData[i] = BigInteger.ZERO;
                }
            }
        }

        if (!useLocalOnly) {
            checkHorizontalSplit(data, localData);
        } else {
            markLocallyPresent(data, localData);
        }
        markActiveRecords(localData);

        this.population = localData.length;
        this.dataStations.put("start", new DataStation(this.serverId, this.localData));

        return response;
    }

    private void markActiveRecords(BigInteger[] localData) {
        if (activeRecords != null && activeRecords.length > 0) {
            for (int i = 0; i < localData.length; i++) {
                if (!recordIsActive(i)) {
                    localData[i] = BigInteger.ZERO;
                }
            }
        }
    }

    private void markLocallyPresent(Data data, BigInteger[] localData) {
        for (int i = 0; i < localData.length; i++) {
            if (!recordIsLocallyPresent(i)) {
                localData[i] = BigInteger.ZERO;
            }
        }
    }

    private void checkHorizontalSplit(Data data) {
        if (data.hasHorizontalSplit()) {
            // if a horizontal split is present, check if this record is locally present.
            // if it is not locally present, treat is as if all local attributes are unknown and set localdata to 1
            // for this record
            Attribute localPresence = data.getData().get(data.getLocalPresenceColumn()).get(0);
            AttributeRequirement checkLocalPresence = new AttributeRequirement();
            checkLocalPresence.setValue(
                    new Attribute(Attribute.AttributeType.bool, "true", localPresence.getAttributeName()));
            List<Attribute> present = data.getData().get(data.getLocalPresenceColumn());
            for (int i = 0; i < localData.length; i++) {
                if (!checkLocalPresence.checkRequirement(present.get(i))) {
                    localData[i] = BigInteger.ONE;
                }
            }
        }
    }

    @GetMapping ("createNodes")
    public NodesResponse createNodes() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < data.getData().size(); i++) {
            if (i == data.getIdColumn() || i == data.getLocalPresenceColumn()) {
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


    private void setMinPercentage(double minPercentage) {
        if (VALID_PERCENTAGES.contains(minPercentage)) {
            this.minPercentage = minPercentage;
        } else {
            this.minPercentage = MINPERCENTAGE_DEFAULT;
        }
    }

    protected Data getData() {
        return data;
    }

    protected Map<String, Set<String>> getUniqueValues() {
        return uniqueValues;
    }

    protected boolean recordIsLocallyPresent(int record) {
        AttributeRequirement req = new AttributeRequirement(
                new Attribute(Attribute.AttributeType.bool, "true", "locallyPresent"));
        if (data.hasHorizontalSplit()) {
            List<Attribute> isLocallyPresent = getData().getAttributeValues("locallyPresent");
            return req.checkRequirement(isLocallyPresent.get(record));
        } else {
            return true;
        }
    }

    protected boolean recordIsActive(int record) {
        return activeRecords[record];
    }
}
