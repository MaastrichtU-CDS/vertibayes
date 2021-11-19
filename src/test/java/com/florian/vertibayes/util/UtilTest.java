package com.florian.vertibayes.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.florian.vertibayes.util.Util.factorial;
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
}