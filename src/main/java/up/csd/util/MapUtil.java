package up.csd.util;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by Smile on 2018/5/21.
 */
public class MapUtil {
    public static Integer getAsInt(Map map, String key) {
        return Integer.valueOf(StringUtils.trim((String)map.get(key)));
    }
    public Long getAsLong(Map map, String key) {
        return Long.valueOf(StringUtils.trim((String)map.get(key)));
    }
    public static String getAsString(Map map, String key) {
        return (String)map.get(key);
    }
}
