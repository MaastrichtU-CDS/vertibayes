package com.florian.vertibayes.encryption;

import java.math.BigInteger;

public class PublicElgamalKey {
    private BigInteger p;
    private BigInteger b;
    private BigInteger c;

    public PublicElgamalKey(BigInteger p, BigInteger b, BigInteger c) {
        this.p = p;
        this.b = b;
        this.c = c;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getB() {
        return b;
    }

    public BigInteger getC() {
        return c;
    }
}
