package up.csd.util;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;


public class FileUtil {

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String LINE_SEPARATOR = (String) System.getProperty("line.separator");

    private static final String MODE_R = "r";
    private static final String MODE_RWS = "rws";

    private FileUtil() {}

    //////////////////////////// convenient methods - load file

    public static String loadFileAsString(String file) {
        return loadFileAsString(file, DEFAULT_CHARSET);
    }

    public static String loadFileAsString(String file, String charset) {
        return loadFileAsString(new File(file), charset);
    }

    public static String loadFileAsString(URL url) {
        return loadFileAsString(url, DEFAULT_CHARSET);
    }

    public static String loadStreamAsString(InputStream is) {
        byte[] bytes = loadStreamAsByteArray(is);
        return newString(bytes, DEFAULT_CHARSET);
    }

    public static String loadStreamAsString(InputStream is, String charset) {
        byte[] bytes = loadStreamAsByteArray(is);
        return newString(bytes, charset);
    }

    public static String loadFileAsString(URL url, String charset) {
        return loadFileAsString(loadFile(url), charset);
    }

    public static String loadFileAsString(File file) {
        return loadFileAsString(file, DEFAULT_CHARSET);
    }

    public static String loadFileAsString(File file, String charset) {
        byte[] content = loadFileAsByteArray(file);
        return newString(content, charset);
    }

    public static List<String> loadFileAsLines(String file) {
        return loadFileAsLines(file, DEFAULT_CHARSET);
    }

    public static List<String> loadFileAsLines(File file) {
        return loadFileAsLines(file, DEFAULT_CHARSET);
    }

    public static List<String> loadFileAsLines(URL url) {
        return loadFileAsLines(loadFile(url), DEFAULT_CHARSET);
    }

    public static List<String> loadFileAsLines(URL url, String charset) {
        return loadFileAsLines(loadFile(url), charset);
    }

    public static List<String> loadFileAsLines(String file, String charset) {
        return loadFileAsLines(new File(file), charset);
    }

    public static byte[] loadFileAsByteArray(URL url) {
        return loadFileAsByteArray(loadFile(url));
    }

    public static byte[] loadFileAsByteArray(String file) {
        return loadFileAsByteArray(new File(file));
    }

    public static Properties loadFileAsProperties(String file) {
        Properties p = new Properties();
        boolean success = loadFileIntoProperties(p, file);
        return success ? p : null;
    }

    public static Properties loadStreamAsProperties(InputStream is) {
        return loadStreamAsProperties(is, DEFAULT_CHARSET);
    }

    public static Properties loadStreamAsProperties(InputStream is, String charset) {
        Properties p = new Properties();
        boolean success = loadStreamIntoProperties(p, is, charset);
        return success ? p : null;
    }

    public static Map<String, String> loadStreamAsConf(InputStream is) {
        List<String> lines = loadStreamAsLines(is, DEFAULT_CHARSET);
        return loadLinesAsConf(lines);
    }

    public static Map<String, String> loadFileAsConf(String file) {
        return loadFileAsConf(new File(file), DEFAULT_CHARSET);
    }

    public static Map<String, String> loadFileAsConf(URL url) {
        return loadFileAsConf(loadFile(url), DEFAULT_CHARSET);
    }

    public static Map<String, String> loadFileAsConf(File file) {
        return loadFileAsConf(file, DEFAULT_CHARSET);
    }

    public static Map<String, String> loadFileAsConf(File file, String charset) {
        return loadLinesAsConf(loadFileAsLines(file, charset));
    }

    public static Properties loadFileAsProperties(URL url) {
        Properties p = new Properties();
        boolean success = loadFileIntoProperties(p, url);
        return success ? p : null;
    }

    public static Properties loadFileAsProperties(File file) {
        Properties p = new Properties();
        boolean success = loadFileIntoProperties(p, file);
        return success ? p : null;
    }

    public static boolean loadFileIntoProperties(Properties p, String file) {
        return loadFileIntoProperties(p, new File(file));
    }

    public static boolean loadFileIntoProperties(Properties p, URL url) {
        return loadFileIntoProperties(p, loadFile(url));
    }

    public static boolean loadFileIntoProperties(Properties p, File file) {
        return loadFileIntoProperties(p, file, DEFAULT_CHARSET);
    }

    public static Properties loadFileAsProperties(String file, String charset) {
        Properties p = new Properties();
        boolean success = loadFileIntoProperties(p, file, charset);
        return success ? p : null;
    }

    public static Properties loadFileAsProperties(File file, String charset) {
        Properties p = new Properties();
        boolean success = loadFileIntoProperties(p, file, charset);
        return success ? p : null;
    }

    public static boolean loadFileIntoProperties(Properties p, String file, String charset) {
        return loadFileIntoProperties(p, new File(file), charset);
    }


    //////////////////////////// convenient methods - save file

    public static void saveBytesAsFile(String file, byte[] content) {
        saveBytesAsFile(new File(file), content);
    }

    public static void saveStringAsFile(String file, String content) {
        saveBytesAsFile(file, toBytes(content));
    }

    public static void saveStringAsFile(File file, String content) {
        saveBytesAsFile(file, toBytes(content));
    }

    public static void saveStringAsFile(String file, String content, String charset) {
        saveBytesAsFile(file, toBytes(content, charset));
    }

    public static void saveLinesAsFile(String file, List<String> content) {
        saveLinesAsFile(file, content.toArray());
    }

    public static void saveLinesAsFile(String file, List<String> content, String charset) {
        saveLinesAsFile(file, content.toArray(), charset);
    }

    public static void saveLinesAsFile(String file, Object[] content) {
        saveLinesAsFile(new File(file), content, DEFAULT_CHARSET);
    }

    public static void saveLinesAsFile(String file, Object[] content, String charset) {
        saveLinesAsFile(new File(file), content, charset);
    }

    public static void saveLinesAsFileWithLineSeperator(String file, List<String> content, String charset, String lineSeperator) {
        saveLinesAsFileWithLineSeperator(file, content.toArray(), charset, lineSeperator);
    }

    public static void saveLinesAsFileWithLineSeperator(String file, String[] content, String charset, String lineSeperator) {
        saveLinesAsFileWithLineSeperator(new File(file), content, charset, lineSeperator);
    }

    public static void saveLinesAsFileWithLineSeperator(String file, Object[] content, String charset, String lineSeperator) {
        saveLinesAsFileWithLineSeperator(new File(file), content, charset, lineSeperator);
    }

    public static void saveLineAsFile(String file, String content) {
        saveLineAsFile(new File(file), content, DEFAULT_CHARSET);
    }

    public static void saveLineAsFile(File file, String content) {
        saveLineAsFile(file, content, DEFAULT_CHARSET);
    }

    public static void saveLineAsFile(String file, String content, String charset) {
        saveLineAsFile(new File(file), content, charset);
    }


    //////////////////////////// convenient methods - append to file

    public static void appendBytesToFile(String file, byte[] content) {
        appendBytesToFile(newRandomAccessFile(file, MODE_RWS), content);
    }

    public static void appendLineToFile(String file, String content) {
        appendStringToFile(file, content + LINE_SEPARATOR, DEFAULT_CHARSET);
    }

    public static void appendStringToFile(String file, String content) {
        appendStringToFile(file, content, DEFAULT_CHARSET);
    }

    public static void appendStringToFile(String file, String content, String charset) {
        appendStringToFile(newRandomAccessFile(file, MODE_RWS), content, charset);
    }

    public static void appendStringToFile(File file, String content, String charset) {
        appendStringToFile(newRandomAccessFile(file, MODE_RWS), content, charset);
    }

    public static void appendStringToFile(RandomAccessFile file, String content, String charset) {
        appendBytesToFile(file, toBytes(content, charset));
    }

    public static void appendLinesToFile(String file, Object[] content) {
        appendLinesToFile(new File(file), content, DEFAULT_CHARSET);
    }

    public static void appendLinesToFile(String file, Collection<Object> lines) {
        appendLinesToFile(new File(file), lines.toArray(new String[0]), DEFAULT_CHARSET);
    }

    public static void appendLinesToFile(File file, Object[] content) {
        appendLinesToFile(file, content, DEFAULT_CHARSET);
    }

    public static void appendLinesToFile(File file, List<Object> lines) {
        appendLinesToFile(file, lines.toArray(), DEFAULT_CHARSET);
    }

    public static void appendLinesToFile(String file, Object[] content, String charset) {
        appendLinesToFile(new File(file), content, charset);
    }

    public static void appendLinesToFile(String file, Collection<Object> lines, String charset) {
        appendLinesToFile(new File(file), lines.toArray(), charset);
    }

    public static void appendLinesToFile(File file, Collection<Object> lines, String charset) {
        appendLinesToFile(file, lines.toArray(), charset);
    }


    //////////////////////////// convenient methods - other

    public static boolean clearFile(String file) {
        return clearFile(new File(file));
    }

    public static long getFileSize(String file) {
        return getFileSize(new File(file));
    }


    //////////////////////////// base operations

    public static String getFileName(String filePath) {
        if (filePath == null) {
            return "";
        }
        int indexOfDot = filePath.lastIndexOf(File.separator);
        if (indexOfDot < 0) {
            return filePath;
        }
        if (indexOfDot + 1 < filePath.length()) {
            return filePath.substring(indexOfDot + 1);
        }
        return "";
    }

    public static Map<String, String> loadLinesAsConf(List<String> lines) {
        Map<String, String> conf = new HashMap<>();
        for (String line : lines) {
            if (StringUtils.isBlank(line) || line.trim().startsWith("#")) {
                continue;
            }
            int index = line.indexOf("=");
            if (index < 0) {
                continue;
            }
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            conf.put(key, value);
        }
        return conf;
    }

    /**
     * 按行加载文件为字符串列表。
     * @param file
     * @param charset
     */
    public static List<String> loadFileAsLines(File file, String charset) {
        try {
            return loadStreamAsLines(new FileInputStream(file), charset);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] loadStreamAsByteArray(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int count;
            byte[] temp = new byte[1024];
            while ((count = in.read(temp, 0, 1024)) != -1) {
                out.write(temp, 0, count);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> loadStreamAsLines(InputStream is, String charset) {

        List<String> lines = new ArrayList<String>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(is, charset));

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(reader);
        }

        return lines;
    }

    /**
     * 加载文件为字节数组。
     * @param file
     */
    public static byte[] loadFileAsByteArray(File file) {
        RandomAccessFile r = newRandomAccessFile(file, MODE_R);

        try {

            byte[] b = new byte[(int) r.length()];
            r.read(b);

            return b;

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(r);
        }
    }

    /**
     * 加载配置文件[.properties]进属性[Properties]对象
     * @param p
     * @param file
     * @param charset
     */
    public static boolean loadFileIntoProperties(Properties p, File file, String charset) {
        try {
            InputStream is = new FileInputStream(file);
            return loadStreamIntoProperties(p, is, charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean loadStreamIntoProperties(Properties p, InputStream is, String charset) {

        try {
            p.load(new InputStreamReader(is, charset)); // 此句依赖JRE6
//            p.load(is);
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(is);
        }
    }

    /**
     * 保存字节数组为一个文件。
     * @param file
     * @param content
     */
    public static void saveBytesAsFile(File file, byte[] content) {

        RandomAccessFile w = newRandomAccessFile(file, MODE_RWS);

        try {
            w.setLength(0);
            w.write(content);

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(w);
        }
    }

    /**
     * 保存一行字符串为一个文件。
     * @param file
     * @param content
     * @param charset
     */
    public static void saveLineAsFile(File file, String content, String charset) {

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false), charset));
            writer.println(content);
            writer.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(writer);
        }
    }

    /**
     * 保存多行字符串为一个文件，指定行尾符。
     * @param file
     * @param content
     * @param charset
     */
    public static void saveLinesAsFileWithLineSeperator(File file, Object[] content, String charset, String lineSeperator) {

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false), charset));
            for (Object line : content) {
                writer.print(line);
                writer.print(lineSeperator);
            }
            writer.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(writer);
        }
    }

    /**
     * 保存多行字符串为一个文件。
     * @param file
     * @param content
     * @param charset
     */
    public static void saveLinesAsFile(File file, Object[] content, String charset) {

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false), charset));
            for (Object line : content) {
                writer.println(line);
            }
            writer.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(writer);
        }
    }

    /**
     * 附加字节到文件末尾。
     * @param file
     * @param content
     */
    public static void appendBytesToFile(RandomAccessFile file, byte[] content) {

        try {
            file.seek(file.length());
            file.write(content);

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(file);
        }
    }

    /**
     * 按行附加对象数组到文件末尾。
     * @param file
     * @param content
     * @param charset
     */
    public static void appendLinesToFile(File file, Object[] lines, String charset) {

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), charset));

            for (Object line : lines) {
                writer.println(line);
            }

            writer.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(writer);
        }
    }

    /**
     * 清空文件
     * @param file
     */
    public static boolean clearFile(File file) {

        RandomAccessFile w = newRandomAccessFile(file, MODE_RWS);

        try {
            w.setLength(0);
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            close(w);
        }
    }

    public static long getFileSize(File file) {
        if (file.exists())
            return file.length();
        return 0;
    }

    //////////////////////////// utility methods

    public static File loadFile(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String newString(byte[] src, String charset) {
        try {
            return new String(src, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toBytes(String src, String charset) {
        try {
            return src.getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toBytes(String src) {
        return toBytes(src, DEFAULT_CHARSET);
    }

    public static RandomAccessFile newRandomAccessFile(File file, String mode) {
        try {
            return new RandomAccessFile(file, mode);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RandomAccessFile newRandomAccessFile(String file, String mode) {
        try {
            return new RandomAccessFile(file, mode);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        String filePath = "D:/repo/svnroot/bz/src/site/web/WEB-INF/vm/admin/home.vm";
        String newFilePath = "D:/repo/svnroot/bz/src/site/web/WEB-INF/vm/admin/home1.vm";
        List<String> content = FileUtil.loadFileAsLines(filePath);
        FileUtil.saveLinesAsFileWithLineSeperator(newFilePath, content, FileUtil.DEFAULT_CHARSET, "\n");
    }
}

