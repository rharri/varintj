package com.github.rharri.varint;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

public class MainTest {

    @Test
    void shouldEncode1() {
        BigInteger n = new BigInteger("1");
        
        Main.EncodingResult encoded = Main.encode(n);

        assertArrayEquals(new byte[] {(byte) 0x01}, encoded.bytes());
    }

    @Test
    void shouldEncode150() {
        BigInteger n = new BigInteger("150");
        
        Main.EncodingResult encoded = Main.encode(n);

        assertArrayEquals(new byte[] {(byte) 0x96, 0x01}, encoded.bytes());
    }

    @Test
    void shouldEncodeMaxUnsignedInt64() {
        BigInteger n = new BigInteger("18446744073709551615");
        
        Main.EncodingResult encoded = Main.encode(n);

        // 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0x01
        byte[] expected = new byte[10];
        for (int i = 0; i < 9; i++) { // set first 9 bytes
            expected[i] = (byte) 0xFF;
        }
        expected[9] = 0x01; // set last byte

        assertArrayEquals(expected, encoded.bytes());
    }

    @Test
    void shouldDecode1() {
        BigInteger n = Main.decode(new byte[] { 0x01 });

        assertEquals(new BigInteger("1"), n);
    }

    @Test
    void shouldDecode150() {
        BigInteger n = Main.decode(new byte[] { (byte) 0x96, 0x01 });

        assertEquals(new BigInteger("150"), n);
    }

    @Test
    void shouldDecodeMaxUnsignedInt64() {
        // 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0x01
        byte[] encodedMaxUInt64 = new byte[10];
        for (int i = 0; i < 9; i++) { // set first 9 bytes
            encodedMaxUInt64[i] = (byte) 0xFF;
        }
        encodedMaxUInt64[9] = 0x01; // set last byte

        BigInteger n = Main.decode(encodedMaxUInt64);

        assertEquals(new BigInteger("18446744073709551615"), n);
    }
}
