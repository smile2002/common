package up.csd.core;

import org.apache.commons.lang.StringUtils;
import up.csd.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Smile on 2018/5/22.
 */
public class EasyMap extends HashMap<String, Object> {

    private static final long serialVersionUID = -4687016913535485435L;
    private int arrayKeyIndex = 0;

	public EasyMap() {
        super();
    }

	public EasyMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

	public EasyMap(int initialCapacity) {
        super(initialCapacity);
    }

    public Integer getAsInt(String key) {
        return Integer.valueOf(getAsTrimmedString(key));
    }

    public Long getAsLong(String key) {
        return Long.valueOf(getAsTrimmedString(key));
    }

    public String getAsString(String key) {
        return (String) super.get(key);
    }


    public byte[] getAsBytes(String key) {
        return (byte[]) super.get(key);
    }

    public String getAsTrimmedString(String key) {
        return StringUtils.trim(getAsString(key));
    }

    public EasyMap getAsEasyMap(String key) {
        return (EasyMap) super.get(key);
    }

    public Object putString(String key, String value) {
        return super.put(key, value);
    }

    public Object putEasyMap(EasyMap value) {
        String key = String.valueOf(arrayKeyIndex);
        ++arrayKeyIndex;
        return super.put(key, value);
    }

    public Object putEasyMap(String key, EasyMap value) {
        return super.put(key, value);
    }

    @Override
    public Object put(String key, Object value) {
        return super.put(key, value);
    }

    /**
     * Remove empty string or sub map in the given map
     */
    public static EasyMap purge(EasyMap map) {
        EasyMap result = new EasyMap(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                String v = (String) entry.getValue();
                // do NOT append empty string
                if (StringUtils.isNotEmpty(v)) {
                    result.put(entry.getKey(), v);
                }
            } else if(entry.getValue() instanceof EasyMap){
                EasyMap m = purge((EasyMap) entry.getValue());
                // do NOT append empty map
                if (!m.isEmpty()) {
                    result.put(entry.getKey(), m);
                }
            }else{
                if(entry.getValue()!=null){
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    public Map<String, Object> toMap() {
        return toMap(this);
    }

    public Map<String, String> toFlatMap() {
        return toFlatMap(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (this != null && this.size() > 0) {
            int i = 0;
            for (Object key : this.keySet()) {
                Object val = this.get(key);
                sb.append(key + "=" + val);
                if (++i < this.size()) {
                    sb.append(",");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static EasyMap fromJson(String json) {
        HashMap map = JsonUtil.fromJson(json, HashMap.class);
        if (map == null) {
            return null;
        }
        return fromMap(map);
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static EasyMap fromMap(Map map) {
        if (map instanceof EasyMap) {
            return (EasyMap) map;
        }
        EasyMap easyMap = new EasyMap();
        for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                value = "";
            } else if (value instanceof String) {
                // do nothing
            } else if (value instanceof Map) {
                EasyMap childMap = fromMap((Map<Object, Object>) value);
                value = childMap;
            } else { // Other types to json
                value = JsonUtil.toJson(value);
            }
            easyMap.put(key.toString(), value);
        }
        return easyMap;
    }

    public static Map<String, Object> toMap(EasyMap nestedMap) {
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : nestedMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof EasyMap) {
                Map<String, Object> childMap = toMap((EasyMap) value);
                value = childMap;
            }
            ret.put(key, value);
        }
        return ret;
    }

    public static Map<String, String> toFlatMap(EasyMap nestedMap) {
        Map<String, String> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : nestedMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!(value instanceof String)) {
                value = JsonUtil.toJson(value);
            }
            ret.put(key, value.toString());
        }
        return ret;
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public void cmd(String cmd) {
        put("cmd", cmd);
    }

    public boolean isSuccess() {
        return "0".equals(getAsString("result"));
    }
}

