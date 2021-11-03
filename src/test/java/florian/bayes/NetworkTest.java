package florian.bayes;

import org.junit.jupiter.api.Test;


class NetworkTest {

    @Test
    public void CreateNetworkTest() {
//        DataOwner station1 = new DataOwner("resources/smallK2Example_firsthalf.csv", "1");
//        DataOwner station2 = new DataOwner("resources/smallK2Example_secondhalf.csv", "2");
//
//        Network network = new Network(Arrays.asList(station1, station2));
//        network.createNetwork();
//        List<Node> nodes = network.getNodes();
//
//        // check if it matches expected network
//        assertEquals(nodes.size(), 3);
//        assertEquals(nodes.get(0).getParents().size(), 0);
//        assertEquals(nodes.get(1).getParents().size(), 1);
//        assertTrue(nodes.get(1).getParents().contains(nodes.get(0)));
//        assertEquals(nodes.get(2).getParents().size(), 1);
//        assertTrue(nodes.get(2).getParents().contains(nodes.get(1)));

        //expected network an example are based on the example in "resources/k2_algorithm.pdf"

    }

    @Test
    public void testDetermineRequirements() {
//        List<Node> nodes = new ArrayList<>();
//        nodes.add(new Node("1", new HashSet<String>() {{
//            add("a");
//            add("b");
//        }}, Attribute.AttributeType.string));
//        nodes.add(new Node("2", new HashSet<String>() {{
//            add("c");
//            add("d");
//        }}, Attribute.AttributeType.string));
//        nodes.add(new Node("3", new HashSet<String>() {{
//            add("e");
//            add("f");
//        }}, Attribute.AttributeType.string));
//
//        Network net = new Network(new ArrayList<>());
//        List<List<Attribute>> requirements = net.determineRequirements(nodes);
//        List<List<Attribute>> expected = new ArrayList<>();
//        List<Attribute> exp = new ArrayList<>();
//        exp.add(new Attribute(Attribute.AttributeType.string, "a", "1"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "c", "2"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "e", "3"));
//        expected.add(exp);
//        exp = new ArrayList<>();
//        exp.add(new Attribute(Attribute.AttributeType.string, "a", "1"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "c", "2"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "f", "3"));
//        expected.add(exp);
//        exp = new ArrayList<>();
//        exp.add(new Attribute(Attribute.AttributeType.string, "a", "1"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "d", "2"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "e", "3"));
//        expected.add(exp);
//        exp = new ArrayList<>();
//        exp.add(new Attribute(Attribute.AttributeType.string, "a", "1"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "d", "2"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "f", "3"));
//        expected.add(exp);
//        exp = new ArrayList<>();
//        exp.add(new Attribute(Attribute.AttributeType.string, "b", "1"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "c", "2"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "e", "3"));
//        expected.add(exp);
//        exp = new ArrayList<>();
//        exp.add(new Attribute(Attribute.AttributeType.string, "b", "1"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "c", "2"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "f", "3"));
//        expected.add(exp);
//        exp = new ArrayList<>();
//        exp.add(new Attribute(Attribute.AttributeType.string, "b", "1"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "d", "2"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "e", "3"));
//        expected.add(exp);
//        exp = new ArrayList<>();
//        exp.add(new Attribute(Attribute.AttributeType.string, "b", "1"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "d", "2"));
//        exp.add(new Attribute(Attribute.AttributeType.string, "f", "3"));
//        expected.add(exp);
//
//        assertEquals(requirements.size(), expected.size());
//        for (List<Attribute> ex : expected) {
//            assertTrue(requirements.contains(exp));
//        }


    }
}