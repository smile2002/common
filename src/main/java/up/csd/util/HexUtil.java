package up.csd.util;

import java.io.UnsupportedEncodingException;

/**
 * Created by Smile on 2018/5/22.
 */
public class HexUtil {
    public static byte[] fromHex(String str) {

        str = str.toUpperCase();

        byte[] bytes = new byte[str.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            byte b1 = (byte) (hexCharToByte(str.charAt(2 * i)) << 4);
            byte b2 = hexCharToByte(str.charAt(2 * i + 1));
            bytes[i] = (byte) (b1 + b2);
        }

        return bytes;
    }

    public static String toHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(byteToHexChar((byte) (bytes[i] >> 4 & 0x0F)));
            sb.append(byteToHexChar((byte) (bytes[i] & 0x0F)));
        }
        return sb.toString();
    }

    private static byte hexCharToByte(char c) {
        if (c >= '0' && c <= '9')
            return (byte) (c - '0');
        else if (c >= 'A' && c <= 'F')
            return (byte) (c - 'A' + 10);
        else
            throw new IllegalArgumentException();
    }

    public static char byteToHexChar(byte b) {
        if (b >= 0 && b <= 9)
            return (char) ('0' + b);
        else if (b >= 10 && b <= 15)
            return (char) ('A' + b - 10);
        else
            throw new IllegalArgumentException();
    }

    public static String md5Hex(String msg) {
        String s = null;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(msg.getBytes());
            byte tmp[] = md.digest();
            s = toHex(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(fromHex("636172644E756D6265723D36323232333130303239333533373035"));
    }
}
