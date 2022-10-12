package com.florian.vertibayes.weka.performance.tests;

import com.florian.vertibayes.weka.performance.tests.util.Performance;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;

import static com.florian.vertibayes.util.PgmxConverter.fromPgmx;
import static com.florian.vertibayes.weka.performance.TestPerformance.printResults;

public class test {
    @Test
    public void test() throws Exception {
        long start = System.currentTimeMillis();
        Performance p = Asia.testVertiBayesFullDataSet();
        p.setName("test");
        printResults(start, p, 0.05, true);

        //79124

    }

    @Test
    public void test2() throws ParserConfigurationException, IOException, SAXException {
        fromPgmx(getPgmx());

    }

    private String getPgmx() throws IOException {
        FileInputStream fis = new FileInputStream("resources/Experiments/model.pgmx");
        return IOUtils.toString(fis, "UTF-8");
    }
}
