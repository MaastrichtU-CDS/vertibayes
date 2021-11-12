package com.florian.vertibayes.encryption;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PublicElgamalTest {

    @Test
    public void testEncryption() {
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


    }
}