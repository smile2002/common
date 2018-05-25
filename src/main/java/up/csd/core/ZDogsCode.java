package up.csd.core;

import com.unionpay.common.jlog.ErrorCode;
import up.csd.util.MiscUtil;

/**
 * Created by Smile on 2018/5/22.
 */
public enum ZDogsCode {

    _1001(1001, "前置异常码"),
    _2001(2001, "服务不可用 [{0}]"),
    _2002(2002, "链路连接失败 [{0}]"),
    _2003(2003, "心跳失败 [{0}]"),
    _2004(2004, "心跳返回异常 [{0}]"),
    _2005(2005, "报文发送失败 [{0}]"),
    _2010(2010, "调用服务超时 [{0}]"),
    _2011(2011, "微信公钥不存在 [{0}]"),
    _3001(3001, "后置异常码"),
    _9001(9001, "节点被隔离");

    private final int code;	// 错误码
    private final String desc; // 错误描述

    private ZDogsCode(int code, String desc) {
        this.code = code;
        this.desc = desc + "[" + MiscUtil.HOST_NM + "]";
    }

    public ErrorCode getErrorCode() {
        return new ErrorCode(this.code);
    }

    public String getDesc() {
        return desc;
    }

    public int getCode() {
        return this.code;
    }
}
