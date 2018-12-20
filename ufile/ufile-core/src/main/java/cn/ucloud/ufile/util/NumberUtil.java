package cn.ucloud.ufile.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 *
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/7 16:39
 */
public class NumberUtil {
    public static byte[] getBytes(short data) {
        return getBytes(data, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] getBytes(short data, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[2]);
        byteBuffer.order(byteOrder);
        byteBuffer.asShortBuffer().put(data);
        return byteBuffer.array();
    }

    public static byte[] getBytes(char data) {
        return getBytes(data, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] getBytes(char data, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[2]);
        byteBuffer.order(byteOrder);
        byteBuffer.asCharBuffer().put(data);
        return byteBuffer.array();
    }

    public static byte[] getBytes(int data) {
        return getBytes(data, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] getBytes(int data, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[4]);
        byteBuffer.order(byteOrder);
        byteBuffer.asIntBuffer().put(data);
        return byteBuffer.array();
    }

    public static byte[] getBytes(long data) {
        return getBytes(data, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] getBytes(long data, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[8]);
        byteBuffer.order(byteOrder);
        byteBuffer.asLongBuffer().put(data);
        return byteBuffer.array();
    }

    public static byte[] getBytes(float data) {
        return getBytes(data, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] getBytes(float data, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[4]);
        byteBuffer.order(byteOrder);
        byteBuffer.asFloatBuffer().put(data);
        return byteBuffer.array();
    }

    public static byte[] getBytes(double data) {
        return getBytes(data, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] getBytes(double data, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[8]);
        byteBuffer.order(byteOrder);
        byteBuffer.asDoubleBuffer().put(data);
        return byteBuffer.array();
    }

    public static short getShort(byte[] bytes) {
        return (short) ((0xff00 & (bytes[0] << 8)) | (0xff & bytes[1]));
    }

    public static char getChar(byte[] bytes) {
        return (char) ((0xff00 & (bytes[0] << 8)) | (0xff & bytes[1]));
    }

    public static int getInt(byte[] bytes) {
        return ((0xff000000 & (bytes[0] << 24)) | (0xff0000 & (bytes[1] << 16)) | (0xff00 & (bytes[2] << 8))
                | (0xff & bytes[3]));
    }

    public static long getLong(byte[] bytes) {
        return (0xffL & (long) bytes[7]) | (0xff00L & ((long) bytes[6] << 8)) | (0xff0000L & ((long) bytes[5] << 16))
                | (0xff000000L & ((long) bytes[4] << 24)) | (0xff00000000L & ((long) bytes[3] << 32))
                | (0xff0000000000L & ((long) bytes[2] << 40)) | (0xff000000000000L & ((long) bytes[1] << 48))
                | (0xff00000000000000L & ((long) bytes[0] << 56));
    }

    public static float getFloat(byte[] bytes) {
        return Float.intBitsToFloat(getInt(bytes));
    }

    public static double getDouble(byte[] bytes) {
        long l = getLong(bytes);
        System.out.println(l);
        return Double.longBitsToDouble(l);
    }

    public static String getString(byte[] bytes, String charsetName) {
        return new String(bytes, Charset.forName(charsetName));
    }
}
