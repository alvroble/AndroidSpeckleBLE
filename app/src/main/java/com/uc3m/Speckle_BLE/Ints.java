package com.uc3m.Speckle_BLE;

import static java.lang.Math.abs;

/**
 * Simplified Ints from Guava
 *
 * @see "https://github.com/google/guava/blob/master/guava/src/com/google/common/primitives/Ints.java"
 **/

public class Ints {

    public static byte[] toByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value
        };
    }

    public static int fromByteArray(byte[] bytes) {
        return fromBytes(bytes[0], bytes[1]);
    }

    private static int fromBytes(byte b1, byte b2) {
        return abs(b2);
    }
}