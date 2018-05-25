package up.csd.json.codec;

import io.netty.util.CharsetUtil;
import up.csd.core.EasyMap;
import up.csd.util.JsonUtil;
import up.csd.util.MapUtil;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by Smile on 2018/5/21.
 */
public class Message {
    public static final Charset CHARSET = CharsetUtil.UTF_8;

    public Header header;
    public byte content[];
    public boolean isPing = false;
    private EasyMap map;

    public Message() { header = new Header(); }
    public RequestId id() { return header.getReqId(); }
    public String cmd() { return toEasyMap().getAsString("cmd"); }
    public String logId() { return toEasyMap().getAsString("__log_id__"); }


    public EasyMap toEasyMap() {
        if (map == null) {
            map = EasyMap.fromJson(new String(content));
        }
        return map;
    }

    public Message buildRespMessage(EasyMap respMap) {
        Message resp = new Message();
        // 构造头
        resp.header = this.header;
        // 填写公共字段
        Map reqMap = this.toEasyMap();
        if (reqMap.containsKey("__log_id__")) {
            respMap.put("__log_id__", reqMap.get("__log_id__"));
        }
        resp.content = JsonUtil.toJson(respMap).getBytes();
        resp.header.setBodyLen(resp.content.length);
        return resp;
    }
}

