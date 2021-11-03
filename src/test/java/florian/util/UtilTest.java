package florian.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static florian.util.Util.factorial;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilTest {

    @Test
    public void factorialTest() {
        BigInteger i = factorial(BigInteger.valueOf(3));
        BigDecimal j = factorial(BigDecimal.valueOf(3.0));

        assertEquals(i, BigInteger.valueOf(6));
        assertEquals(j, BigDecimal.valueOf(6));
    }
}