package florian.bayes.data;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class Parser {

    private Parser() {
    }

    public static Data parseCsv(String path, int idColumn) {
        List<List<String>> records = readCsv(path);
        List<String> types = records.get(0);
        List<String> attributes = records.get(1);
        List<List<Attribute>> parsed = new ArrayList<>();

        for (int i = 0; i < attributes.size(); i++) {
            List<Attribute> attribute = new ArrayList<>();
            for (int j = 2; j < records.size(); j++) {
                attribute.add(new Attribute(Attribute.AttributeType.valueOf(types.get(i)), records.get(j).get(i),
                                            attributes.get(i)));
            }
            parsed.add(attribute);
        }

        Data data = new Data(idColumn, parsed);
        return data;
    }

    private static List<List<String>> readCsv(String path) {
        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader(path))) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (CsvValidationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
}
