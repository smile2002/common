package up.csd.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.util.CharsetUtil;
/**
 * Created by Smile on 2018/5/22.
 */
public class MiscUtil {

    private static Log logger = LogFactory.getLog(MiscUtil.class);

    public static final String HOST_NM;

    static {
        try {
            HOST_NM = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            throw new RuntimeException("Init HOST_NM failed!!", e);
        }
    }

    private static final transient AtomicInteger ID = new AtomicInteger(new Random().nextInt(19999)*100000+((int)(System.currentTimeMillis()/3600000))%100000);

    public static byte[] MSG_KEEP_ALIVE = new byte[] {0x30, 0x30, 0x30, 0x30};

    public static synchronized int nextId(){
        ID.compareAndSet(2000000000, 100000);
        return ID.incrementAndGet();
    }

    public static void waitFor(long time) {
        if (time <= 0) {
            return;
        }
        try {
//        	logger.debug("Util.waitFor(time) is waiting for " + printUsedTime(time));
            Thread.sleep(time);
        } catch (Exception e) {
            logger.warn("Exception happend. <- " + e, e);
        }
    }

    public static String printUsedTimeFromStartTime(long startTime) {
        long time = System.currentTimeMillis() - startTime;
        return printUsedTime(time);
    }

    public static String printUsedTimeFromStartTimeChs(long startTime) {
        long time = System.currentTimeMillis() - startTime;
        return printUsedTimeChs(time);
    }

    public static String printUsedTime(long time) {
        StringBuilder sb = new StringBuilder();
        time = appendTime(sb, time, 24 * 60 * 60 * 1000, "d "); // day
        time = appendTime(sb, time, 60 * 60 * 1000, "h "); // hr
        time = appendTime(sb, time, 60 * 1000, "'"); // min
        time = appendTime(sb, time, 1000, "\""); // sec
        time = appendTime(sb, time, 1, ""); // ms
        return sb.toString();
    }

    public static String printUsedTimeChs(long time) {
        StringBuilder sb = new StringBuilder();
        time = appendTime(sb, time, 24 * 60 * 60 * 1000, "天"); // day
        time = appendTime(sb, time, 60 * 60 * 1000, "小时"); // hr
        time = appendTime(sb, time, 60 * 1000, "分"); // min
        time = appendTime(sb, time, 1000, "秒"); // sec
        time = appendTime(sb, time, 1, ""); // ms
        return sb.toString();
    }

    private static long appendTime(StringBuilder sb, long time, long countPerUnit, String unit) {
        Validate.notNull(sb);
        if (time >= countPerUnit) {
            sb.append(time/countPerUnit);
            sb.append(unit);
            return time % countPerUnit;
        }

        if (sb.length() > 0 || countPerUnit < 60001) {
            sb.append("0");
            sb.append(unit);
        }
        return time;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> clone(HashMap<K, V> orignal) {
        return (HashMap<K, V>) orignal.clone();
    }

    public static <K, V> ConcurrentHashMap<K, V> clone(ConcurrentHashMap<K, V> orignal) {
        ConcurrentHashMap<K, V> newOne = new ConcurrentHashMap<>();
        newOne.putAll(orignal);
        return newOne;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> clone(LinkedHashMap<K, V> orignal) {
        return (LinkedHashMap<K, V>) orignal.clone();
    }

    public static byte[] pin2PinBlockWithCardNo(String pin, String cardNo) {
        byte[] tPinByte = pin2PinBlock(pin);
        if (cardNo.length() == 11) {
            cardNo = "00" + cardNo;
        } else if (cardNo.length() == 12) {
            cardNo = "0" + cardNo;
        }
        byte[] tPanByte = formatPan(cardNo);
        byte[] tByte = new byte[8];
        for (int i = 0; i < 8; i++) {
            tByte[i] = (byte) (tPinByte[i] ^ tPanByte[i]);
        }
        return tByte;
    }

    private static byte[] pin2PinBlock(String aPin) {
        int tTemp = 1;
        int tPinLen = aPin.length();

        byte[] tByte = new byte[8];
        try {
            /*******************************************************************
             * if (tPinLen > 9) { tByte[0] = (byte) Integer.parseInt(new
             * Integer(tPinLen) .toString(), 16); } else { tByte[0] = (byte)
             * Integer.parseInt(new Integer(tPinLen) .toString(), 10); }
             ******************************************************************/
//			tByte[0] = (byte) Integer.parseInt(new Integer(tPinLen).toString(),
//					10);
            tByte[0] = (byte) Integer.parseInt(Integer.toString(tPinLen), 10);
            if (tPinLen % 2 == 0) {
                for (int i = 0; i < tPinLen;) {
                    String a = aPin.substring(i, i + 2);
                    tByte[tTemp] = (byte) Integer.parseInt(a, 16);
                    if (i == (tPinLen - 2)) {
                        if (tTemp < 7) {
                            for (int x = (tTemp + 1); x < 8; x++) {
                                tByte[x] = (byte) 0xff;
                            }
                        }
                    }
                    tTemp++;
                    i = i + 2;
                }
            } else {
                for (int i = 0; i < tPinLen - 1;) {
                    String a;
                    a = aPin.substring(i, i + 2);
                    tByte[tTemp] = (byte) Integer.parseInt(a, 16);
                    if (i == (tPinLen - 3)) {
                        String b = aPin.substring(tPinLen - 1) + "F";
                        tByte[tTemp + 1] = (byte) Integer.parseInt(b, 16);
                        if ((tTemp + 1) < 7) {
                            for (int x = (tTemp + 2); x < 8; x++) {
                                tByte[x] = (byte) 0xff;
                            }
                        }
                    }
                    tTemp++;
                    i = i + 2;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return tByte;
    }

    private static byte[] formatPan(String aPan) {
        int tPanLen = aPan.length();
        byte[] tByte = new byte[8];
        ;
        int temp = tPanLen - 13;
        try {
            tByte[0] = (byte) 0x00;
            tByte[1] = (byte) 0x00;
            for (int i = 2; i < 8; i++) {
                String a = aPan.substring(temp, temp + 2);
                tByte[i] = (byte) Integer.parseInt(a, 16);
                temp = temp + 2;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tByte;
    }

    public static byte[] readBytesFromInputStream(InputStream is, int msgLength) throws Exception {

        byte[] message = new byte[msgLength];
        int readLength = 0; // 读到的字节长度

        while (readLength != msgLength) {

            int readLengthThisTime = is.read(message, readLength, msgLength - readLength); // 尝试读TCP数据包

            if (readLengthThisTime == -1) { // TODO
                throw new RuntimeException("AsyncClient stream closed abnormally.");
            }

            readLength += readLengthThisTime;
        }

        return message;
    }

    public static int read2ByteInteger(InputStream is) throws Exception {
        return new BigInteger(readBytesFromInputStream(is, 2)).intValue();
    }

    public static int read4ByteInteger(InputStream is) throws Exception {
        return new BigInteger(readBytesFromInputStream(is, 4)).intValue();
    }

    public static int read4ByteASCInteger(InputStream is) throws Exception {
        return Integer.valueOf(readASCIIStringFromInputStream(is, 4));
    }

    public static String readASCIIStringFromInputStream(InputStream is, int length) throws Exception {
        return readStringFromInputStream(is, length, "ASCII");
    }

    public static String readUTF8StringFromInputStream(InputStream is, int length) throws Exception {
        return readStringFromInputStream(is, length, "UTF-8");
    }

    public static String readStringFromInputStream(InputStream is, int length, String charset) throws Exception {
        return new String(readBytesFromInputStream(is, length), charset);
    }

    public static byte[] textToNumericFormatIpV4(String ip) {

        String[] addrStr = ip.trim().split("\\.");
        byte[] addr = {
                Integer.valueOf(addrStr[0]).byteValue(),
                Integer.valueOf(addrStr[1]).byteValue(),
                Integer.valueOf(addrStr[2]).byteValue(),
                Integer.valueOf(addrStr[3]).byteValue()
        };

        return addr;
    }

    /**
     * 整形转4字节网络字节序
     */
    public static byte[] htonl(int h) {

        byte[] nl = new byte[4];
        nl[0] = (byte) ((h >>> 24) & 0xFF);
        nl[1] = (byte) ((h >>> 16) & 0xFF);
        nl[2] = (byte) ((h >>>  8) & 0xFF);
        nl[3] = (byte) ((h >>>  0) & 0xFF);

        return nl;
    }

    /**
     * 将长度左补零到4位
     * @param length
     * @return
     */
    public static byte[] getResponseLengthString(int length) {
        return intToStr(length, 4);
    }

    public static String joinMap(Map<?, ?> map) {
        return joinMapUsingURLEncodedValue(map, null);
    }

    public static String joinMapUsingURLEncodedValue(Map<?, ?> map, Charset charset) {
        StringBuilder b = new StringBuilder();
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() == null) {
                    b.append(urlEncode(entry.getKey().toString(), charset));
                    b.append('=');
                    b.append('&');
                } else {
                    b.append(urlEncode(entry.getKey().toString(), charset));
                    b.append('=');
                    b.append(urlEncode(entry.getValue().toString(), charset));
                    b.append('&');
                }
            }
            b.deleteCharAt(b.length() - 1);
        }
        return b.toString();
    }

    public static String urlEncode(String src, Charset charset) {
        if (charset == null) {
            return src;
        }
        try {
            return URLEncoder.encode(src, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String urlDecode(String src, Charset charset) {
        if (charset == null) {
            return src;
        }
        try {
            return URLDecoder.decode(src, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析ACP应答字符串，生成MAP
     */
    public static Map<String, String> parseQString(String str) {

        Map<String, String> map = new HashMap<String, String>();
        int len = str.length();
        StringBuilder temp = new StringBuilder();
        char curChar;
        String key = null;
        boolean isKey = true;
        boolean isOpen = false;// 值里有嵌套
        char openName = 0;
        if (len > 0) {
            for (int i = 0; i < len; i++) {// 遍历整个带解析的字符串
                curChar = str.charAt(i);// 取当前字符
                if (isKey) {// 如果当前生成的是key

                    if (curChar == '=') {// 如果读取到=分隔符
                        key = temp.toString();
                        temp.setLength(0);
                        isKey = false;
                    } else {
                        temp.append(curChar);
                    }
                } else {// 如果当前生成的是value
                    if (isOpen) {
                        if (curChar == openName) {
                            isOpen = false;
                        }

                    } else {// 如果没开启嵌套
                        if (curChar == '{') {// 如果碰到，就开启嵌套
                            isOpen = true;
                            openName = '}';
                        }
                        if (curChar == '[') {
                            isOpen = true;
                            openName = ']';
                        }
                    }
                    if (curChar == '&' && !isOpen) {// 如果读取到&分割符,同时这个分割符不是值域，这时将map里添加
                        putKeyValueToMap(temp, isKey, key, map);
                        temp.setLength(0);
                        isKey = true;
                    } else {
                        temp.append(curChar);
                    }
                }
            }
            putKeyValueToMap(temp, isKey, key, map);
        }
        return map;
    }

    private static void putKeyValueToMap(StringBuilder temp, boolean isKey, String key, Map<String, String> map) {
        if (isKey) {
            key = temp.toString();
            if (key.length() == 0) {
                throw new RuntimeException("QString format illegal");
            }
            map.put(key, "");
        } else {
            if (key.length() == 0) {
                throw new RuntimeException("QString format illegal");
            }
            map.put(key, temp.toString());
        }
    }

    /**
     * 将长度转成字符串并左补零到strLen长度
     * @param number
     * @param strLen
     */
    public static byte[] intToStr(int number, int strLen) {
        StringBuilder sb = new StringBuilder();
        sb.append(number);
        if (sb.length() < strLen) {
            int i = strLen - sb.length();
            while (i-- > 0) {
                sb.insert(0, "0");
            }
        }
        return sb.toString().getBytes();
    }

    public static byte[] join(byte[]... bytesArray) {
        int totalLength = 0;
        for (byte[] bytes : bytesArray) {
            totalLength += bytes.length;
        }
        byte[] dest = new byte[totalLength];
        int destPos = 0;
        for (byte[] bytes : bytesArray) {
            System.arraycopy(bytes, 0, dest, destPos, bytes.length);
            destPos += bytes.length;
        }
        return dest;
    }

    public static byte[] md5All(byte[]... src) {
        return md5(join(src));
    }

    public static byte[] md5(byte[] src){

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(src);

            return messageDigest.digest();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String b64(String str) {
        return Base64.encodeBase64String(str.getBytes(CharsetUtil.UTF_8));
    }

    public String unb64(String str) {
        return new String(Base64.decodeBase64(str));
    }

    public String hex2B64(String hex) {
        return new String(Base64.encodeBase64(HexUtil.fromHex(hex)));
    }

    public static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName(); // format: "pid@hostname"
        return name.substring(0, name.indexOf('@'));
    }

    public static void savePid() {
        String userDir = System.getProperty("user.dir");
        String homeDir = System.getProperty("user.home");
        String md5Val = HexUtil.toHex(md5(userDir.getBytes(CharsetUtil.ISO_8859_1))).toLowerCase();
        String pidFile = homeDir + "/." + md5Val + ".pid";
        System.out.println("pid file is: " + pidFile);
        FileUtil.saveStringAsFile(pidFile, MiscUtil.getPid());
    }

    /** 压缩 + Base64编码 */
    public static byte[] deflateEncode(byte[] raw) {
        if (raw == null || raw.length == 0) {
            throw new IllegalArgumentException("Ecode and deflate error with null raw byte[]");
        }
        byte[] tmpByte = deflater(raw);
        return Base64.encodeBase64(tmpByte);
    }

    /** Base64解码 + 解压缩 */
    public static byte[] decodeInflate(String raw) {
        if (raw == null || raw.length() == 0) {
            throw new IllegalArgumentException("Decode and inflate error with null raw byte[]");
        }
        byte[] tmpByte = Base64.decodeBase64(raw.getBytes());
        return inflater(tmpByte);
    }

    /**
     * Deflate算法压缩字节数组
     * @param inputByte
     * @return byte[]
     */
    public static byte[] deflater(byte[] inputByte)  {
        int compressedDataLength = 0;
        Deflater compresser = new Deflater();
        compresser.setInput(inputByte);
        compresser.finish();
        ByteArrayOutputStream o = new ByteArrayOutputStream(inputByte.length);
        byte[] result = new byte[1024];
        try {
            while (!compresser.finished()) {
                compressedDataLength = compresser.deflate(result);
                o.write(result, 0, compressedDataLength);
            }
        } finally {
            try {
                o.close();
                compresser.end();
            } catch(Exception e) {
                throw new RuntimeException("", e);
            }
        }
        return o.toByteArray();
    }

    public static byte[] inflater(byte[] inputByte) {
        int compressedDataLength = 0;
        Inflater compresser = new Inflater(false);
        compresser.setInput(inputByte, 0, inputByte.length);
        ByteArrayOutputStream o = new ByteArrayOutputStream(inputByte.length);
        byte[] result = new byte[1024];
        try {
            while (!compresser.finished()) {
                compressedDataLength = compresser.inflate(result);
                if (compressedDataLength == 0) {
                    break;
                }
                o.write(result, 0, compressedDataLength);
            }
        } catch (DataFormatException e) {
            throw new RuntimeException("decompress catch data format exception", e);
        }  finally {
            try {
                o.close();
                compresser.end();
            } catch (IOException e) {
                throw new RuntimeException("", e);
            }
        }
        return o.toByteArray();
    }

    /**
     * 批量v2.0报文上送为xml格式（MPIReq=...&Version=2.0.0）
     * */
    public static String joinMap2Xml(Map<?, ?> map, String version, Charset charset) {
        StringBuilder b = new StringBuilder();
        b.append("MPIReq=");
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<ACQGATE>").append("<Message id=\"msg_id\">");
        sb.append("<MPIReq id=\"sig_id\">");
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() == null) {
                    sb.append('<').append(entry.getKey().toString()).append('>');
                    sb.append("</").append(entry.getKey().toString()).append('>');
                } else {
                    sb.append('<').append(entry.getKey().toString()).append('>');
                    sb.append(entry.getValue().toString());
                    sb.append("</").append(entry.getKey().toString()).append('>');
                }
            }
        }
        sb.append("</MPIReq>");
        sb.append("</Message>").append("</ACQGATE>");

        byte[] data = MiscUtil.deflateEncode(sb.toString().getBytes(charset));
        String MPIReq = new String(data, charset);
        // 顺序换一下，先base64压缩再URLEncode
        MPIReq=urlEncode(MPIReq,charset);

        b.append(MPIReq).append('&').append("Version=").append(version);
        return b.toString();
    }

    public static long cost(long beginNanoTime, TimeUnit... timeUnit) {
        try {
            TimeUnit myTimeUnit = TimeUnit.MILLISECONDS;
            if(timeUnit != null && timeUnit.length > 0) {
                myTimeUnit = timeUnit[0];
            }

            long duration = (System.nanoTime() - beginNanoTime);
            return myTimeUnit.convert(duration, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            return -1;
        }
    }
    //////////////////////////////////////////////// begin of 时间相关工具
    public static String yyyy() {
        return new SimpleDateFormat("yyyy").format(new Date());
    }

    public static String yy() {
        return new SimpleDateFormat("yy").format(new Date());
    }

    public static String yyyyMM() {
        return new SimpleDateFormat("yyyyMM").format(new Date());
    }

    public static String yyyyMMdd() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    public static String yyMMdd() {
        return new SimpleDateFormat("yyMMdd").format(new Date());
    }

    public static String MMdd() {
        return new SimpleDateFormat("MMdd").format(new Date());
    }

    public static String lastMonth_MMdd() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        return new SimpleDateFormat("MMdd").format(c.getTime());
    }

    public static String nextMonth_MMdd() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        return new SimpleDateFormat("MMdd").format(c.getTime());
    }

    public static String HHmmss() {
        return new SimpleDateFormat("HHmmss").format(new Date());
    }

    public static String yesterday_yyyyMMddHHmmss() {
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        return new SimpleDateFormat("yyyyMMddHHmmss").format(yesterday);
    }

    public static String yesterday_MMdd() {
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        return new SimpleDateFormat("MMdd").format(yesterday);
    }

    public static String yyyyMMddHHmmss() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public static String tomorrow_yyyyMMddHHmmss() {
        Date tomorrow = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        return new SimpleDateFormat("yyyyMMddHHmmss").format(tomorrow);
    }

    public static String MMddHHmmss() {
        return new SimpleDateFormat("MMddHHmmss").format(new Date());
    }

    public static String yyyyMMddHHmmssSSS() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    public static String currentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String currentTimeStampMillis() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    public static long currentTimeMillis() {
        return new Date().getTime();
    }

    public static long currentUnixTimeStamp() {
        return new Date().getTime() / 1000;
    }
    //////////////////////////////////////////////// end of 时间相关工具
}
