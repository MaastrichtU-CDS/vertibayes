package com.florian.vertibayes.notunittests.generatedata;

import com.florian.vertibayes.bayes.data.Attribute;
import com.florian.vertibayes.bayes.data.Data;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.florian.vertibayes.bayes.data.Parser.parseCsv;

public class GenerateMissingData {
    private static final String CSV_PATH_IRIS_ORIGINAL = "resources/Experiments/iris/iris.csv";
    private static final String CSV_PATH_IRIS_TARGET = "resources/Experiments/iris/irisMissing.csv";

    private static final String CSV_PATH_ASIA_ORIGINAL = "resources/Experiments/asia/Asia10k.csv";
    private static final String CSV_PATH_ASIA_TARGET = "resources/Experiments/asia/asiaMissing.csv";

    private static final String CSV_PATH_ALARM_ORIGINAL = "resources/Experiments/alarm/ALARM10k.csv";
    private static final String CSV_PATH_ALARM_TARGET = "resources/Experiments/alarm/alarmMissing.csv";

    private static final double TRESHOLD = 0.05;

    @Test
    public void generateMissingData() {
        generateMissingData(CSV_PATH_IRIS_ORIGINAL, CSV_PATH_IRIS_TARGET);
        generateMissingData(CSV_PATH_ASIA_ORIGINAL, CSV_PATH_ASIA_TARGET);
        generateMissingData(CSV_PATH_ALARM_ORIGINAL, CSV_PATH_ALARM_TARGET);
        System.out.println();
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
        for (List<Attribute> a : d.getData()) {
            if (s.length() > 1) {
                s += ",";
            }
            s += a.get(0).getAttributeName();
        }
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

    private void printCSV(List<String> data, String path) {
        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            data.stream()
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
