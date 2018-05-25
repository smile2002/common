package up.csd.util;

/**
 * Created by Smile on 2018/5/21.
 */

public class ByteUtil {

    public static boolean bigEndian = false;

    public static void short2bytes(byte[] bytes, int offset, int value) {
        short2bytes(bytes, offset, value, bigEndian);
    }

    public static void short2bytes(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) ((value >>> 8) & 0xFF);
            bytes[offset + 1] = (byte) ((value >>> 0) & 0xFF);
        } else {
            bytes[offset + 1] = (byte) ((value >>> 8) & 0xFF);
            bytes[offset] = (byte) ((value >>> 0) & 0xFF);
        }
    }

    public static void int2bytes(byte[] bytes, int offset, int value) {
        int2bytes(bytes, offset, value, bigEndian);
    }

    public static void int2bytes(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) ((value >>> 24) & 0xFF);
            bytes[offset + 1] = (byte) ((value >>> 16) & 0xFF);
            bytes[offset + 2] = (byte) ((value >>> 8) & 0xFF);
            bytes[offset + 3] = (byte) ((value >>> 0) & 0xFF);
        } else {
            bytes[offset + 3] = (byte) ((value >>> 24) & 0xFF);
            bytes[offset + 2] = (byte) ((value >>> 16) & 0xFF);
            bytes[offset + 1] = (byte) ((value >>> 8) & 0xFF);
            bytes[offset] = (byte) (((byte) (value >>> 0)) & (byte) 0xFF);
        }
    }

    @Deprecated
    public static  void arraycopy(byte[] dest, int offset, byte[] src, int soffset, int len) {
        int i = 0;
        for (i = 0; i < len; i++) {
            dest[offset + i] = src[soffset + i];
        }
    }

    public static  int bytes2short(byte[] bytes, int offset){
        return bytes2short(bytes, offset, bigEndian);
    }

    public static  int bytes2short(byte[] bytes, int offset, boolean bigEndian) {
        if (bigEndian) {
            return (changeUnsignedByte(bytes[offset]) << 8) + (changeUnsignedByte(bytes[offset + 1]) << 0);
        } else {
            return (changeUnsignedByte(bytes[offset + 1]) << 8) + changeUnsignedByte((bytes[offset]) << 0);
        }
    }

    public static  int bytes2int(byte[] bytes, int offset){
        return bytes2int(bytes, offset, bigEndian);
    }

    public static  int bytes2int(byte[] bytes, int offset, boolean bigEndian) {
        if (bigEndian) {
            return (changeUnsignedByte(bytes[offset]) << 24) + (changeUnsignedByte(bytes[offset + 1]) << 16)
                    + (changeUnsignedByte(bytes[offset + 2]) << 8) + (changeUnsignedByte(bytes[offset + 3]) << 0);
        } else {
            return (changeUnsignedByte(bytes[offset]) << 0) + (changeUnsignedByte(bytes[offset + 1]) << 8)
                    + (changeUnsignedByte(bytes[offset + 2]) << 16) + (changeUnsignedByte(bytes[offset + 3]) << 24);
        }
    }

    public static  int changeUnsignedByte(int signedByte) {
        int unsignedByte = signedByte >= 0 ? signedByte : signedByte + 256;
        return unsignedByte;

    }
}

