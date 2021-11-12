package com.florian.vertibayes.encryption;

import java.math.BigInteger;

public class EncryptedElgamal {
    private BigInteger a;
    private BigInteger b;

    public BigInteger getA() {
        return a;
    }

    public void setA(BigInteger a) {
        this.a = a;
    }

    public BigInteger getB() {
        return b;
    }

    public void setB(BigInteger b) {
        this.b = b;
    }

    public static EncryptedElgamal multiply(EncryptedElgamal a, EncryptedElgamal b) {
        EncryptedElgamal res = new EncryptedElgamal();
        res.setA(a.getA().multiply(b.getA()));
        res.setB(a.getB().multiply(b.getB()));
        return res;
    }
}