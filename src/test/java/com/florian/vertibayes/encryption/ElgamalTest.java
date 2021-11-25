package com.florian.vertibayes.encryption;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElgamalTest {
    private static int PRECISION = 10000;

    @Test
    public void testElgamal() {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                BigInteger a = BigInteger.valueOf(i);
                BigInteger b = BigInteger.valueOf(j);

                Elgamal main = new Elgamal();
                main.generateKeys();
                Elgamal second = new Elgamal(main.getPublicKey());

                EncryptedElgamal eA = main.encrypt(a);
                EncryptedElgamal eB = second.encrypt(b);

                EncryptedElgamal multiple = EncryptedElgamal.multiply(eA, eB);
                assertEquals(main.decrypt(multiple), a.multiply(b));
            }
        }

        for (double i = 0; i < 1; i += 0.1) {
            for (double j = 0; j < 1; j += 0.1) {
                BigInteger a = BigInteger.valueOf((int) (i * PRECISION));
                BigInteger b = BigInteger.valueOf((int) (j * PRECISION));

                Elgamal main = new Elgamal();
                main.generateKeys();
                Elgamal second = new Elgamal(main.getPublicKey());

                EncryptedElgamal eA = main.encrypt(a);
                EncryptedElgamal eB = second.encrypt(b);

                EncryptedElgamal multiple = EncryptedElgamal.multiply(eA, eB);
                double decrypted = main.decrypt(multiple).doubleValue() / (PRECISION * PRECISION);

                assertEquals(decrypted, i * j, 0.001);
            }
        }
    }
}