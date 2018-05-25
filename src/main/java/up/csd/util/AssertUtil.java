package up.csd.util;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Smile on 2018/5/22.
 */
public class AssertUtil {

    /**
     * 验证方法传入的参数是否为null，如果是则抛非法参数异常，异常信息可以自己指定
     *
     * @param o
     * @param msg
     */
    public static void argIsNotNull(Object o, String msg) {
        if (o == null) {
            if (StringUtil.isBlank(msg)) {
                msg = "参数为null";
            }
            throw new RuntimeException(msg);
        }
    }

    /**
     * 验证字符串是否为null或空，如果是则抛如果是则抛非法参数异常，异常信息可以自己指定
     *
     * @param str
     */
    public static void argIsNotBlank(String str, String msg) {
        if (StringUtil.isBlank(str)) {
            if (StringUtil.isBlank(msg)) {
                msg = "字符串为空: " + str;
            }
            throw new RuntimeException(msg);
        }
    }

    /**
     * 对象如果是null则抛运行时异常，异常信息可以自己指定
     *
     * @param o
     * @param msg
     */
    public static void objIsNotNull(Object o, String msg) {
        if (o == null) {
            if (StringUtil.isBlank(msg)) {
                msg = "对象为null";
            }
            throw new RuntimeException(msg);
        }
    }

    /**
     * 对象如果不是null则抛运行时异常，异常信息可以自己指定
     *
     * @param o
     * @param msg
     */
    public static void objIsNull(Object o, String msg) {
        if (o != null) {
            if (StringUtil.isBlank(msg)) {
                msg = "对象不为null";
            }
            throw new RuntimeException(msg);
        }
    }

    /**
     * 验证对象是否为null，如果是null，则抛运行时异常，异常信息可以自己指定
     *
     * @param o
     * @param msg
     */
    public static void strIsNotBlank(String str, String msg) {
        if (StringUtil.isBlank(str)) {
            if (StringUtil.isBlank(msg)) {
                msg = "字符串为空: " + str;
            }
            throw new RuntimeException(msg);
        }
    }

    public static void arrIsNotEmpty(Object[] arr, String msg) {
        objIsNotNull(arr, "obj is null");
        if (arr.length == 0) {
            if (StringUtil.isBlank(msg)) {
                msg = "数组为空";
            }
            throw new RuntimeException(msg);
        }
    }

    public static void collectionIsNotEmpty(Collection<?> lst, String msg) {
        if (lst == null || lst.size() == 0) {
            if (StringUtil.isBlank(msg)) {
                msg = "集合为空";
            }
            throw new RuntimeException(msg);
        }
    }

    public static void mapIsNotEmpty(Map<?, ?> mp, String msg) {
        if (mp == null || mp.size() == 0) {
            if (StringUtil.isBlank(msg)) {
                msg = "Map为空";
            }
            throw new RuntimeException(msg);
        }
    }

    /**
     * 断言数组包含某元素
     *
     * @param arr
     * @param ojb
     * @param msg
     */
    public static void arrContains(Object[] arr, Object obj, String msg) {
        objIsNotNull(arr, "arr is null");
        arrIsNotEmpty(arr, "arr is empty");
        objIsNotNull(obj, "obj is null");

        boolean b = false;
        for (Object o : arr) {
            if (o.equals(obj)) {
                b = true;
            }
        }
        if (!b) {
            if (StringUtil.isBlank(msg)) {
                msg = "数组不包含该对象:" + obj;
            }
            throw new RuntimeException(msg);
        }
    }

    /**
     * 两个字符串不相等
     *
     * @param s1
     * @param s2
     * @param msg
     */
    public static void strEquals(String s1, String s2, String msg) {
        strIsNotBlank(s1, "s1 is blank");
        strIsNotBlank(s2, "s2 is blank");
        if (!s1.equals(s2)) {
            if (StringUtil.isBlank(msg)) {
                throw new RuntimeException("s1 is not equals to s2");
            } else {
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * 两个字符串相等
     *
     * @param s1
     * @param s2
     * @param msg
     */
    public static void strNotEqual(String s1, String s2, String msg) {
        strIsNotBlank(s1, "s1 is blank");
        strIsNotBlank(s2, "s2 is blank");
        if (s1.equals(s2)) {
            if (StringUtil.isBlank(msg)) {
                throw new RuntimeException("s1 is not equals to s2");
            } else {
                throw new RuntimeException(msg);
            }
        }
    }

}
