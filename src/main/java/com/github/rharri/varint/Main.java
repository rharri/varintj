package com.github.rharri.varint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main 
{
    static record Stats(BigInteger n, int bytesBefore, int bytesAfter) {}
    
    static record EncodingResult(byte[] bytes, Stats stats) {}

    // 10 bytes
    private static int MAX_VARINT = 10;

    // 0b01111111
    private static int PAYLOAD_BITS = 7;

    // 0x7F
    private static BigInteger LOW_ORDER_7_BITS = new BigInteger("127");

    public static void main(String[] args) throws IOException {
        if (args.length == 0 || args[0].isEmpty() || args[0].isBlank()) {
            System.out.println("missing file operand");
            System.exit(1);
        }

        String fileName = args[0];

        byte[] bytes = Files.readAllBytes(Path.of(fileName));

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        BigInteger n = new BigInteger(
            1, buffer.order(ByteOrder.BIG_ENDIAN).array());
        
        EncodingResult encoded = encode(n);

        // Turn on assertions with vm arg -ea
        assert decode(encoded.bytes()).equals(n);

        System.out.println(encoded.stats());
        System.out.println("OK");

        System.exit(0);
    }

    static EncodingResult encode(BigInteger n) {
        BigInteger src = n;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(10);
        while (true) {
            byte b = n.and(LOW_ORDER_7_BITS).byteValue();

            n = n.shiftRight(PAYLOAD_BITS);

            if (n.compareTo(BigInteger.ZERO) > 0) {
                b |= 0x80;
                buffer.write(b);
            } else {
                buffer.write(b);
                break;
            }
        }

        assert buffer.size() <= MAX_VARINT;

        return new EncodingResult(
            buffer.toByteArray(), new Stats(src, Long.BYTES, buffer.size()));
    }

    static BigInteger decode(byte[] varint) {
        BigInteger n = BigInteger.ZERO;
        int byteIndex = 0;
        for (byte b : varint) {
            byte[] masked = { (byte)(b & 0x7F) };
            BigInteger m = new BigInteger(masked);
            n = n.add(m.shiftLeft(byteIndex * PAYLOAD_BITS));
            ++byteIndex;
        }
        return n;
    }
}
