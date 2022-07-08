package com.florian.vertibayes.weka.performance.tests;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.vertibayes.webservice.domain.external.WebNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.florian.vertibayes.notunittests.generatedata.GenerateNetworks.createWebNode;
import static com.florian.vertibayes.weka.performance.tests.util.VertiBayesPerformance.generateModel;

public class RuntimeTest {

    public static final String TEST_FULL = "resources/Experiments/k2/k2.arff";
    public static final String TEST_FULL_TEN_VALUES = "resources/Experiments/k2/k2_tenValues.arff";
    public static final String TEST_FULL_FOUR_VALUES = "resources/Experiments/k2/k2_fourValues.arff";

    public static final String FIRSTHALF_SMALL = "resources/Experiments/k2/smallK2Example_firsthalf.csv";
    public static final String SECONDHALF_SMALL = "resources/Experiments/k2/smallK2Example_secondhalf.csv";

    public static final String FIRSTHALF_BIG = "resources/Experiments/k2/bigK2Example_firsthalf.csv";
    public static final String SECONDHALF_BIG = "resources/Experiments/k2/bigK2Example_secondhalf.csv";

    public static final String FIRSTHALF_BIG_TEN_VALUES = "resources/Experiments/k2" +
            "/bigK2Example_firsthalf_tenValues.csv";
    public static final String FIRSTHALF_BIG_FOUR_VALUES = "resources/Experiments/k2" +
            "/bigK2Example_firsthalf_fourValues.csv";
    private static final String LABEL = "x3";

    private static final double MINPERCENTAGE = 0.1;


    public static void testSmallDataset() throws Exception {
        List<WebNode> nodes = threeNode();
        generateModel(nodes, FIRSTHALF_SMALL, SECONDHALF_SMALL, LABEL, MINPERCENTAGE);


    }

    public static void testRegularDataset() throws Exception {
        List<WebNode> nodes = threeNode();
        generateModel(nodes, FIRSTHALF_BIG, SECONDHALF_BIG, LABEL, MINPERCENTAGE);
    }

    public static void testRegularDatasetMultipleParents() throws Exception {
        List<WebNode> nodes = threeNodeMultipleParents();
        generateModel(nodes, FIRSTHALF_BIG, SECONDHALF_BIG, LABEL, MINPERCENTAGE);
    }

    public static void testRegularDatasetTenValues() throws Exception {
        List<WebNode> nodes = threeNode();
        generateModel(nodes, FIRSTHALF_BIG_TEN_VALUES, SECONDHALF_BIG, LABEL, MINPERCENTAGE);
    }

    public static void testRegularDatasetFourValues() throws Exception {
        List<WebNode> nodes = threeNode();
        generateModel(nodes, FIRSTHALF_BIG_FOUR_VALUES, SECONDHALF_BIG, LABEL, MINPERCENTAGE);
    }

    public static List<WebNode> threeNodeMultipleParents() {
        WebNode x1 = createWebNode("x1", Attribute.AttributeType.string, new ArrayList<>());
        WebNode x2 = createWebNode("x2", Attribute.AttributeType.string, Arrays.asList(x1.getName()));
        WebNode x3 = createWebNode("x3", Attribute.AttributeType.string, Arrays.asList(x1.getName(), x2.getName()));

        return Arrays.asList(x1, x2, x3);
    }

    public static List<WebNode> threeNode() {
        WebNode x1 = createWebNode("x1", Attribute.AttributeType.string, new ArrayList<>());
        WebNode x2 = createWebNode("x2", Attribute.AttributeType.string, Arrays.asList(x1.getName()));
        WebNode x3 = createWebNode("x3", Attribute.AttributeType.string, Arrays.asList(x2.getName()));

        return Arrays.asList(x1, x2, x3);
    }
}
