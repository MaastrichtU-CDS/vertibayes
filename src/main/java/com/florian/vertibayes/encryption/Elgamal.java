package com.florian.vertibayes.encryption;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class Elgamal {
    private BigInteger privateKey;
    private PublicElgamalKey publicKey;
    private Random sc = new SecureRandom();
    private int bits = 8;

    public Elgamal() {
    }

    public Elgamal(PublicElgamalKey publicKey) {
        this.publicKey = publicKey;
    }

    public PublicElgamalKey getPublicKey() {
        return publicKey;
    }

    public void generateKeys() {
        privateKey = new BigInteger(bits, sc);
        BigInteger p = BigInteger.probablePrime(bits, sc);
        BigInteger b = new BigInteger("3");
        BigInteger c = b.modPow(privateKey, p);
        publicKey = new PublicElgamalKey(p, b, c);
    }


    public EncryptedElgamal encrypt(BigInteger i) {
        BigInteger r = new BigInteger(bits, sc);
        BigInteger EC = i.multiply(publicKey.getC().modPow(r, publicKey.getP())).mod(publicKey.getP());
        BigInteger brmodp = publicKey.getB().modPow(r, publicKey.getP());
        EncryptedElgamal e = new EncryptedElgamal();
        e.setA(EC);
        e.setB(brmodp);
        return e;
    }

    public BigInteger decrypt(EncryptedElgamal e) {
        BigInteger crmodp = e.getB().modPow(privateKey, publicKey.getP());
        BigInteger d = crmodp.modInverse(publicKey.getP());
        return d.multiply(e.getA()).mod(publicKey.getP());
    }


}
