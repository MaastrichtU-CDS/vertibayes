package com.florian.vertibayes.util;

import com.florian.vertibayes.bayes.data.Attribute;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class MathUtil {

    public static final int PRECISION = 3;
    public static final int INT = 0;

    private MathUtil() {
    }

    public static BigInteger factorial(BigInteger i) {
        BigInteger res = BigInteger.ONE;
        for (BigInteger j = BigInteger.valueOf(2); j.compareTo(i) <= INT; j = j.add(BigInteger.ONE)) {
            res = res.multiply(j);
        }
        return res;
    }

    public static BigDecimal factorial(BigDecimal i) {
        BigDecimal res = BigDecimal.ONE;
        for (BigDecimal j = BigDecimal.valueOf(2); j.compareTo(i) <= INT; j = j.add(BigDecimal.ONE)) {
            res = res.multiply(j);
        }
        return res;
    }

    public static double round(double value, Attribute.AttributeType type) {
        int decimals = type == Attribute.AttributeType.numeric ? INT : PRECISION;
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}
