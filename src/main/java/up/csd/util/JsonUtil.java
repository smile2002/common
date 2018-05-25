package up.csd.util;

import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

/**
 * Created by Smile on 2018/5/21.
 */
public class JsonUtil {

    private static final Logger logger = Logger.getLogger("sys.log");

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper formattedMapper = new ObjectMapper();
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    static {
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, false);
        mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
//        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
//        mapper.getSerializationConfig().withDateFormat(new SimpleDateFormat(DATE_FORMAT));
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        formattedMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        formattedMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        formattedMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
//        formattedMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
//        formattedMapper.getSerializationConfig().withDateFormat(new SimpleDateFormat(DATE_FORMAT));
        formattedMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Class<T> t) {

        if (json == null) {
            return null;
        }
        try {
            return mapper.readValue(json, t);
        } catch (Exception e) {
            logger.info("Cannot parse json: <" + json + ">, Object class: <" + t.getName() + ">.", e);
        }
        return null;
    }

    public static <T> T fromJson(String json, TypeReference<T> t) {

        if (json == null) {
            return null;
        }

        json = json.trim();
        if ((json.startsWith("{") && !json.endsWith("}")) || (json.startsWith("[") && !json.endsWith("]"))) {
            throw new RuntimeException("Illegal JSON <" + json + ">");
        }

        try {
            return mapper.readValue(json, t);
        } catch (Exception e) {
            logger.info("Cannot parse json: <" + json + ">, Object class: <" + t.getType() + ">.", e);
        }
        return null;
    }

    public static <T> T fromJsonWithException(String json, Class<T> t) {
        try {
            return mapper.readValue(json, t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJsonWithException(String json, TypeReference<T> t) {
        try {
            return mapper.readValue(json, t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromMap(Map<?, ?> map, Class<T> t) {

        if (map == null) {
            return null;
        }
        try {
            return mapper.readValue(toJson(map), t);
        } catch (Exception e) {
            logger.info("Cannot parse map: <" + map + ">, Object class: <" + t.getName() + ">.", e);
        }
        return null;
    }

    public static <T> List<T> fromMapList(List<Map<String, Object>> mapList) {

        if (mapList == null) {
            return null;
        }
        try {
            return mapper.readValue(toJson(mapList), new TypeReference<List<T>>(){});
        } catch (Exception e) {
            logger.info("Cannot parse mapList: <" + mapList + ">.", e);
        }

        return null;
    }

    public static <T> T fromURL(URL url, Class<T> t) {
        return fromJson(FileUtil.loadFileAsString(url), t);
    }

    public static <T> T fromURL(URL url, TypeReference<T> t) {
        return fromJson(FileUtil.loadFileAsString(url), t);
    }

    public static <T> T fromStream(InputStream is, Class<T> t) {
        return fromJson(FileUtil.loadStreamAsString(is), t);
    }

    public static <T> T fromStream(InputStream is, TypeReference<T> t) {
        return fromJson(FileUtil.loadStreamAsString(is), t);
    }

    public static <T> T fromFile(String file, TypeReference<T> t) {

        File f = new File(file);

        if (!f.exists() || !f.isFile()) {
            logger.warn("File[" + file + "] does not exist.");
            return null;
        }

        return fromJson(FileUtil.loadFileAsString(file), t);
    }

    public static <T> T fromFile(String file, Class<T> t) {
        Validate.notEmpty(file);
        return fromFile(new File(file), t);
    }

    public static <T> T fromFile(File f, Class<T> t) {

        Validate.notNull(f);

        if (!f.exists() || !f.isFile()) {
            logger.warn("File[" + f + "] does not exist.");
            return null;
        }

        return fromJson(FileUtil.loadFileAsString(f), t);
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn(e);
        }
        return "{}";
    }

    public static String toFormattedJson(Object obj) {
        return toJson(obj, true);
    }

    public static String toJson(Object obj, boolean formatOutput) {
        try {
            if (formatOutput) {
                return formattedMapper.writeValueAsString(obj);
            } else {
                return mapper.writeValueAsString(obj);
            }
        } catch (Exception e) {
            logger.warn(e);
        }
        return "{}";
    }
}

