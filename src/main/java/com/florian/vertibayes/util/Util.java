package com.florian.vertibayes.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class Util {

    private Util() {
    }

    public static BigInteger factorial(BigInteger i) {
        BigInteger res = BigInteger.ONE;
        for (BigInteger j = BigInteger.valueOf(2); j.compareTo(i) <= 0; j = j.add(BigInteger.ONE)) {
            res = res.multiply(j);
        }
        return res;
    }

    public static BigDecimal factorial(BigDecimal i) {
        BigDecimal res = BigDecimal.ONE;
        for (BigDecimal j = BigDecimal.valueOf(2); j.compareTo(i) <= 0; j = j.add(BigDecimal.ONE)) {
            res = res.multiply(j);
        }
        return res;
    }
}
