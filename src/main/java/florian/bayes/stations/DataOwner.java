package florian.bayes.stations;

import com.florian.station.DataStation;
import florian.bayes.Node;
import florian.bayes.data.Attribute;
import florian.bayes.data.Data;

import java.math.BigInteger;
import java.util.*;

import static florian.bayes.data.Parser.parseCsv;

public class DataOwner {
    private Data data;
    private Map<String, Set<String>> uniqueValues = new HashMap<>();
    private String id;
    private DataStation a;


    public DataOwner(String path, String id) {
        this.data = parseCsv(path, 0);
        for (String name : data.getCollumnIds().keySet()) {
            uniqueValues.put(name, Data.getUniqueValues(data.getAttributeValues(name)));
        }
        this.id = id;
    }

    public void createStation(List<Attribute> requirements) {
        int population = data.getNumberOfIndividuals();
        BigInteger[][] matrix = new BigInteger[population][population];
        for (int i = 0; i < population; i++) {
            for (int j = 0; j < population; j++) {
                matrix[i][j] = BigInteger.ONE;
            }
        }

        List<List<Attribute>> values = data.getData();
        for (Attribute req : requirements) {
            if (data.getAttributeCollumn(req.getAttributeName()) == null) {
                // attribute not locally available, skip
                continue;
            }
            for (int i = 0; i < population; i++) {
                if (!values.get(data.getAttributeCollumn(req.getAttributeName())).get(i).equals(req)) {
                    matrix[i][i] = BigInteger.ZERO;
                }
            }
        }
    }


    public List<Node> createNodes() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < data.getData().size(); i++) {
            if (i == data.getIdColumn()) {
                continue;
            }
            Attribute attribute = data.getData().get(i).get(0);
            nodes.add(new Node(attribute.getAttributeName(), uniqueValues.get(attribute.getAttributeName()),
                               attribute.getType()));
        }
        return nodes;
    }
}
