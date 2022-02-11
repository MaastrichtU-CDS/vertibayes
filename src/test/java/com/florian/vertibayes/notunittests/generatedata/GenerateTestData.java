package com.florian.vertibayes.notunittests.generatedata;

import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.bayes.data.Data;
import org.apache.commons.collections.map.HashedMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.florian.vertibayes.bayes.data.Parser.parseCsv;
import static com.florian.vertibayes.util.PrintingPress.printARFF;
import static com.florian.vertibayes.util.PrintingPress.printCSV;

public class GenerateTestData {
    private static final String CSV_PATH_IRIS_ORIGINAL = "resources/Experiments/iris/iris.csv";
    private static final String CSV_PATH_IRIS_MISSING = "resources/Experiments/iris/irisMissing.csv";
    private static final String CSV_PATH_IRIS_TARGET = "resources/Experiments/iris/folds/iris";

    private static final String CSV_PATH_ASIA_ORIGINAL = "resources/Experiments/asia/Asia10k.csv";
    private static final String CSV_PATH_ASIA_MISSING = "resources/Experiments/asia/Asia10kMissing.csv";
    private static final String CSV_PATH_ASIA_TARGET = "resources/Experiments/asia/folds/asia";

    private static final String CSV_PATH_ALARM_ORIGINAL = "resources/Experiments/alarm/ALARM10k.csv";
    private static final String CSV_PATH_ALARM_MISSING = "resources/Experiments/alarm/ALARM10kMissing.csv";
    private static final String CSV_PATH_ALARM_TARGET = "resources/Experiments/alarm/folds/alarm";

    private static final double TRESHOLD = 0.05;
    private static final Integer FOLDS = 10;

    @Test
    public void generateMissingData() {
        //IMPORTANT NOTE: GENERATED DATA MAY NOT HAVE THE SAME FORM AS THE FINAL WEKA MODEL
        //CHECK IF ALL ATTRIBUTES ARE IN THE SAME ORDER, IDEM FOR ATTRIBUTE-VALUES IN THE CASE OF NOMINAL ATTRIBUTES

        generateMissingData(CSV_PATH_IRIS_ORIGINAL, CSV_PATH_IRIS_MISSING);
        generateMissingData(CSV_PATH_ASIA_ORIGINAL, CSV_PATH_ASIA_MISSING);
        generateMissingData(CSV_PATH_ALARM_ORIGINAL, CSV_PATH_ALARM_MISSING);
        generateFolds(CSV_PATH_IRIS_MISSING, CSV_PATH_IRIS_TARGET, FOLDS, true);
        generateFolds(CSV_PATH_ASIA_MISSING, CSV_PATH_ASIA_TARGET, FOLDS, true);
        generateFolds(CSV_PATH_ALARM_MISSING, CSV_PATH_ALARM_TARGET, FOLDS, true);
        generateFolds(CSV_PATH_IRIS_ORIGINAL, CSV_PATH_IRIS_TARGET, FOLDS, false);
        generateFolds(CSV_PATH_ASIA_ORIGINAL, CSV_PATH_ASIA_TARGET, FOLDS, false);
        generateFolds(CSV_PATH_ALARM_ORIGINAL, CSV_PATH_ALARM_TARGET, FOLDS, false);
    }

    private void generateFolds(String path, String target, int folds, boolean missing) {
        Data d = parseCsv(path, 0);

        Map<Integer, List<Attribute>> folded = new HashedMap();
        List<Attribute> ids = d.getIds();
        for (int i = 0; i < folds; i++) {
            folded.put(i, new ArrayList<>());
        }
        Random r = new Random();
        for (Attribute id : ids) {
            boolean done = false;

            while (!done) {
                int fold = ((int) (r.nextDouble() * 10.0));
                if (folded.get(fold).size() < ids.size() / folds) {
                    folded.get(fold).add(id);
                    done = true;
                }
            }
        }

        if (missing) {
            target += "missing";
        }
        printFold(target, d, folded);
        createVerticalSplit(d, target, folded);

    }

    private void createVerticalSplit(Data d, String target, Map<Integer, List<Attribute>> folded) {
        List<List<Attribute>> leftSplit = new ArrayList<>();
        List<List<Attribute>> rightSplit = new ArrayList<>();
        leftSplit.add(d.getData().get(d.getAttributeCollumn("ID")));
        rightSplit.add(d.getData().get(d.getAttributeCollumn("ID")));
        for (int i = 0; i < d.getData().size(); i++) {
            if (i == d.getAttributeCollumn("ID")) {
                continue;
            }
            if (i % 2 == 0) {
                leftSplit.add(d.getData().get(i));
            } else {
                rightSplit.add(d.getData().get(i));
            }
        }
        Data leftSplitD = new Data(0, leftSplit);
        Data rightSplitD = new Data(0, rightSplit);

        printCombinedFolds(target + "RightSplit", rightSplitD, folded);
        printCombinedFolds(target + "LeftSplit", leftSplitD, folded);


    }

    private void printCombinedFolds(String target, Data d, Map<Integer, List<Attribute>> folded) {
        for (Integer key : folded.keySet()) {
            List<Integer> otherFolds = folded.keySet().stream().filter(x -> x != key).collect(Collectors.toList());
            List<String> data = new ArrayList<>();
            String s = "";

            String header = "";
            for (List<Attribute> a : d.getData()) {
                if (s.length() >= 1) {
                    s += ",";
                }

                if (header.length() >= 1) {
                    header += ",";
                }
                header += a.get(0).getType();

                s += a.get(0).getAttributeName();

            }
            data.add(header);
            data.add(s);

            for (Integer otherKey : otherFolds) {
                for (Attribute id : folded.get(otherKey)) {
                    int row = d.getIndividualRow(id.getValue());
                    s = "";

                    for (List<Attribute> attribute : d.getData()) {
                        if (s.length() >= 1) {
                            s += ",";
                        }

                        s += attribute.get(row).getValue();
                    }
                    data.add(s);
                }
            }
            String ids = otherFolds.stream().sorted().collect(Collectors.toList()).toString().replace("[", "")
                    .replace("]", "").replace(" ", "").replace(",", "");

            printCSV(data, target + ids + ".csv");
        }
    }


    private void printFold(String target, Data d, Map<Integer, List<Attribute>> folded) {
        for (Integer key : folded.keySet()) {
            List<String> data = new ArrayList<>();
            List<String> dataWeka = new ArrayList<>();
            List<String> arff = new ArrayList<>();
            String s = "";
            String sWeka = "";
            String header = "";
            for (List<Attribute> a : d.getData()) {
                if (s.length() >= 1) {
                    s += ",";
                }
                if (sWeka.length() >= 1) {
                    sWeka += ",";
                }
                if (header.length() >= 1) {
                    header += ",";
                }
                header += a.get(0).getType();
                if (!a.get(0).getAttributeName().equals("ID")) {
                    sWeka += a.get(0).getAttributeName();
                }
                s += a.get(0).getAttributeName();
            }
            data.add(header);
            data.add(s);
            dataWeka.add(sWeka);
            for (Attribute id : folded.get(key)) {
                int row = d.getIndividualRow(id.getValue());
                s = "";
                sWeka = "";
                for (List<Attribute> attribute : d.getData()) {
                    if (s.length() >= 1) {
                        s += ",";
                    }
                    if (sWeka.length() >= 1) {
                        sWeka += ",";
                    }
                    if (!attribute.get(0).getAttributeName().equals("ID")) {
                        sWeka += attribute.get(row).getValue();
                    }
                    s += attribute.get(row).getValue();
                }
                arff.add(sWeka);
                dataWeka.add(sWeka);
                data.add(s);
            }
            printCSV(data, target + key + ".csv");
            printCSV(dataWeka, target + key + "WEKA.csv");
            printARRF(d, arff, key, target + key + "WEKA.arff");
        }
    }

    private void printARRF(Data d, List<String> wekaData, int key, String path) {
        List<String> data = new ArrayList<>();
        String s = "@Relation genericBIFF";
        data.add(s);

        for (List<Attribute> a : d.getData()) {
            if (a.get(0).getAttributeName().equals("ID")) {
                continue;
            }
            s = "";
            s += "@Attribute";
            s += " " + a.get(0).getAttributeName() + " ";
            if (a.get(0).getType() == Attribute.AttributeType.string) {
                s += "{";
                int count = 0;
                List<String> uniqueValues = new ArrayList<>();
                for (Attribute att : a) {
                    if (!uniqueValues.contains(att.getValue())) {
                        uniqueValues.add(att.getValue());
                    }
                }
                for (String unique : uniqueValues) {
                    if (!unique.equals("?")) {
                        //only print valid values here, otherwiseweka will think ? is also valid.
                        if (count > 0) {
                            s += ",";
                        }
                        count++;
                        s += unique;
                    }
                }
                s += "}";
            } else {
                s += a.get(0).getType();
            }
            data.add(s);
        }

        data.add("");
        data.add("@DATA");

        data.addAll(wekaData);
        printARFF(data, path);
    }

    private void generateMissingData(String path, String target) {
        Data d = parseCsv(path, 0);
        Random r = new Random();
        for (List<Attribute> attribute : d.getData()) {
            for (Attribute a : attribute) {
                if (a.getAttributeName().equals("ID")) {
                    break;
                } else {
                    if (r.nextDouble() < TRESHOLD) {
                        a.setValue("?");
                    }
                }
            }
        }

        List<String> data = new ArrayList<>();
        String s = "";
        String header = "";
        for (List<Attribute> a : d.getData()) {
            if (s.length() >= 1) {
                s += ",";
            }
            if (header.length() >= 1) {
                header += ",";
            }
            header += a.get(0).getType();
            s += a.get(0).getAttributeName();
        }
        data.add(header);
        data.add(s);
        for (int i = 0; i < d.getNumberOfIndividuals(); i++) {
            s = "";
            for (List<Attribute> a : d.getData()) {
                if (s.length() >= 1) {
                    s += ",";
                }
                s += a.get(i).getValue();
            }
            data.add(s);
        }

        printCSV(data, target);
    }
}
