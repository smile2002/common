package sandbox;

import up.csd.core.EasyMap;
import up.csd.util.JsonUtil;

import java.util.HashMap;

/**
 * Created by Smile on 2018/5/21.
 */
public class JsonTest {
    public static void main0(String[] args) {
        String json = "{\"key1\":\"value1\", \"key2\":\"value2\", \"key3\":{\"field1\":\"param1\",\"field2\":\"param2\" }, \"key4\":[ {\"nm1\":\"prop1\"}, {\"nm2\":\"prop2\"} ] }";
        HashMap map = JsonUtil.fromJson(json, HashMap.class);
        System.out.println(map.toString());
        System.out.println(map.getClass().toString());
        Object value1 = map.get("key1"); System.out.println(value1.getClass().toString());
        Object value3 = map.get("key3"); System.out.println(value3.getClass().toString());
        Object value4 = map.get("key4"); System.out.println(value4.getClass().toString());
    }
    public static void main(String[] args) {
        String json = "{\"key1\":\"value1\", \"key2\":\"value2\", \"key3\":{\"field1\":\"param1\",\"field2\":\"param2\" }, \"key4\":[ {\"nm1\":\"prop1\"}, {\"nm2\":\"prop2\"} ] }";
        EasyMap map = EasyMap.fromJson(json);
        System.out.println(map.toString());
    }
}
