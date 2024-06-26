package com.florian.vertibayes.util;

import com.florian.nscalarproduct.data.Attribute;
import com.florian.vertibayes.bayes.Bin;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import static com.florian.vertibayes.util.MathUtil.factorial;
import static com.florian.vertibayes.util.MathUtil.round;
import static com.florian.vertibayes.util.Util.mapBins;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {
    private static int PRECISION = 10000;

    @Test
    public void testFactorial() {
        BigInteger i = factorial(BigInteger.valueOf(3));
        BigDecimal j = factorial(BigDecimal.valueOf(3.0));

        assertEquals(i, BigInteger.valueOf(6));
        assertEquals(j, BigDecimal.valueOf(6));
    }

    @Test
    public void testBinMapper() {
        Set<Bin> bins = new HashSet<>();
        Bin b = new Bin();
        b.setUpperLimit("1");
        b.setLowerLimit("0");
        bins.add(b);

        assertEquals(((Bin) mapBins(bins).toArray()[0]).getUpperLimit(), "1");
        //pointless test for coverage
        bins.remove(b);
        assertEquals(mapBins(bins).toArray().length, 0);
    }

    @Test
    public void testRound() {
        double x = 0.1234;
        double y = 0.1235;
        double z = 0.5;

        double xRound = round(x, Attribute.AttributeType.real);
        assertEquals(0.123, xRound);
        double yRound = round(y, Attribute.AttributeType.real);
        assertEquals(0.124, yRound);

        double xInt = round(x, Attribute.AttributeType.numeric);
        assertEquals(0, xInt);
        double zInt = round(z, Attribute.AttributeType.numeric);
        assertEquals(1, zInt);
    }
}