package up.csd.util;

import org.apache.commons.lang.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Smile on 2018/5/22.
 */
public class StringUtil extends org.apache.commons.lang.StringUtils {

    public static final String DEFAULT_SPLITER = ",";

    public static final String ZH_REGEX = "[\u4e00-\u9fa5]"; // 中文字符正则表达式
    public static final String URL_REGEX =	"^(http://){0,1}.+\\..+\\..+$"; // URL正则表达式
    public static final String EMAIL_REGEX = // EMAIL正则表达式
            "\\b^['_a-z0-9-\\+]+(\\.['_a-z0-9-\\+]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.([a-z]{2}|aero|arpa|asia|biz|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|nato|net|org|pro|tel|travel|xxx)$\\b";
    public static final String NUM_REGEX = "^[0-9]+$"; // 整数正则表达式
    public static final String NUM_WORD_REGEX = "^[A-Za-z0-9]+$"; // 数字字符正则表达式
    public static final String MOBILE_REGEX = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\\\d{8}$"; // 手机号正则表达式
    public static final String WORD_REGEX = "^[A-Za-z]+$"; // 字母正则表达式
    public static final String AMT_REGEX = "^(([1-9]\\d{0,9})|0)(\\.\\d{1,2})?$"; // 金额正则表达式
    public static final String HEX_REGEX = "^[0-9A-Fa-f]+$";
    public static final String PWD_REGEX = "^[A-Za-z0-9!@#$*()_+^&}{:?.]+$";
    public static final String DATE_STR_REGEX = "^\\d{4}[0-1]{1}[0-9]{1}[0-3]{1}[0-9]{1}$";
    public static final String DATE_STR_YYYYMMDD_REGEX = "^(\\d{4})(0\\d{1}|1[0-2])(0\\d{1}|[12]\\d{1}|3[01])$";
    public static final String CERTIF_TP_REGEX = "^0[1-7]|99$";
    public static final String ACC_NO_REGEX = "^(\\d{16,19}|(?:[a-z0-9+/]{4})*(?:[a-z0-9+/]{2}==|[a-z0-9+/]{3}=)?)$";
    public static final String CERTIF_CARD_ID_REGEX = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$|^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";
    public static final String SPECIAL_CHARACTER_REGEX = "^[0-9a-zA-Z\\s?]+$";
    public static final String NOT_CHAR_REGEX = "[^a-zA-Z]+";
    public static final String[] mons = new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    private static final DecimalFormat DEFAULT_DECIMAL_FORMATTER = new DecimalFormat("#.##");

    /**
     * 将字符串数组trim后拼接成字符串，用默认的分隔符进行分隔
     * @param strArray
     * @return
     */
    public static String strArrToString(String[] strArr) {
        AssertUtil.argIsNotNull(strArr, "参数strArr为null");
        return strArrToString(strArr, DEFAULT_SPLITER);
    }

    /**
     * 将数组trim后拼接成字符串,用指定的分隔符进行分隔
     * @param strArray
     * @return
     */
    public static String strArrToString(String[] strArr, String spliter) {
        AssertUtil.argIsNotNull(strArr, "参数strArr为null");

        // 如果未指定分隔符，或者指定的分隔符字符串为空，则用默认的分隔符进行分隔
        if (isBlank(spliter)) {
            spliter = DEFAULT_SPLITER;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strArr.length; i ++) {
            sb.append(strArr[i].trim());
            if (i < strArr.length - 1) {
                sb.append(spliter);
            }
        }
        return sb.toString();
    }

    /**
     * 将以下划线分隔的字符串按驼峰命令法进行转换
     * @param strOrg
     * @return
     */
    public static String camelCase(String strOrg) {
        String[] list = StringUtil.lowerCase(strOrg).split("_");
        StringBuilder buf = new StringBuilder(strOrg.length());
        for(String word : list) {
            buf.append(word);
            int firstLetterIndex = buf.length()-word.length();
            if( firstLetterIndex != 0) {
                buf.setCharAt(firstLetterIndex, Character.toUpperCase(buf.charAt(firstLetterIndex)));
            }
        }
        return buf.toString();
    }

    /**
     * 格式化输出msg
     * @param msg
     * @param args
     * @return
     */
    public static String formatMessage(String msg, Object... args) {
        if (isBlank(msg) || ArrayUtils.getLength(args) == 0) {
            return msg;
        } else {
            return MessageFormat.format(msg, args);
        }
    }

    /**
     * 根据指定长度初始化CHAR_MAP
     * @param length
     * @return
     */
    public static String initCharMap(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i ++) {
            sb.append("0");
        }
        return sb.toString();
    }

    /**
     * 将Set转成逗号分隔的字符串
     * @param strSet
     * @return
     */
    public static String setToString(Set<String> strSet) {
        if (strSet != null && !strSet.isEmpty()) {
            return strArrToString(strSet.toArray(new String[] { }));
        }
        return "";
    }

    /**
     * 将Set转成按指定字符分隔的字符串
     * @param strSet
     * @return
     */
    public static String setToString(Set<String> strSet, String spliter) {
        if (strSet != null && !strSet.isEmpty()) {
            return strArrToString(strSet.toArray(new String[] { }), spliter);
        }
        return "";
    }

    /**
     * 检查是否包含中文信息
     * @param str
     * @return
     */
    public static boolean containsZhCn(String str) {
        if (isBlank(str)) {
            return false;
        }
        Matcher matcher = Pattern.compile(ZH_REGEX).matcher(str);
        return matcher.find();
    }

    /**
     * 计算字符串长度，如果是中文，则要乘以3
     * @param str
     * @return
     */
    public static int strLen(String str, String charSet) {
        int len = 0;
        if (containsZhCn(str)) {
            try {
                if (StringUtil.isBlank(charSet)) {
                    charSet = "UTF-8";
                }
                len = str.getBytes(charSet).length;
            } catch (Exception e) {
                throw new IllegalStateException("获取字符串长度失败" + str);
            }
        } else {
            len = str.length();
        }
        return len;
    }


    /**
     * 获取指定位值
     * @param ctrl
     * @param bitIdx
     * @return
     */
    public static String getValueOfBit(String ctrl, int bitIdx, String defaultBitValue) {
        if (StringUtil.isBlank(ctrl)) {
            return defaultBitValue;
        }
        if(bitIdx <0 || bitIdx >= ctrl.length() ){
            return defaultBitValue;
        }
        return ctrl.substring(bitIdx, bitIdx+1);
    }

    public static String strDefaultVal(String val, String defaultVal) {
        if (isBlank(defaultVal)) {
            defaultVal = "";
        }
        return isBlank(val) ? defaultVal : val;
    }

    public static Date strToDate(String str) {

        if(!isBlank(str)) {
            Date date = null;
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = sdf.parse(str);
            } catch (ParseException e){
                throw new IllegalStateException("日期字符串格式出错：" + str);
            }
            return date;
        }else {
            throw new IllegalStateException("日期字符串格式为空！");
        }
    }

    /**
     * Description: 将字符串由UTF-8转换成GBK编码格式
     * @author QiTing  2014-5-12
     * @param str
     * @return
     */
    public static String UTF8ToGBK(String str) {
        return new String(str.getBytes(), Charset.forName("GBK"));
    }

    /**
     * 传入源字符串与期望截取的长度，按照GBK编码格式进行截取
     * @param originStr
     * @param truncateLength
     * @return
     */
    public static String truncateString(String originStr,int truncateLength){
        if(originStr.length() <= truncateLength/2) {
            return originStr;
        } else {
            int byteLength = originStr.getBytes(Charset.forName("GBK")).length;
            while(byteLength - truncateLength > 0) {

                int endPosition = originStr.length() - (int)Math.ceil(((float)(byteLength - truncateLength)/2));
                originStr = originStr.substring(0, endPosition);
                byteLength = originStr.getBytes(Charset.forName("GBK")).length;
            }
            return originStr;
        }
    }

    /**
     * 字符串匹配正则表达式列表（20111102）
     * @param srcStr
     * @param regexArr
     * @return 字符串能匹配一个正则表达式就返回true，否则返回false
     */
    public static  boolean matchRegexs(String srcStr, String[] regexArr) {
        AssertUtil.strIsNotBlank(srcStr, "字符串为空");
        AssertUtil.objIsNotNull(regexArr, "正则表达式数组为null");
        for (String regex : regexArr) {
            if (srcStr.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchRegex(String srcStr, String regex) {
        AssertUtil.strIsNotBlank(srcStr, "字符串为空");
        AssertUtil.strIsNotBlank(regex, "正则表达式为空");
        return srcStr.matches(regex);
    }

    public static String concat(String spliter, String... strs) {
        StringBuilder sb = new StringBuilder();
        if (spliter == null) {
            spliter = ",";
        }
        if (strs != null && strs.length > 0) {
            for (int i = 0; i < strs.length; i ++) {
                sb.append(strs[i]);
                if (i < strs.length - 1) {
                    sb.append(spliter);
                }
            }
        }
        return sb.toString();
    }

    public static String concatSet(String spliter, Set<String> st) {
        StringBuilder sb = new StringBuilder();
        if (spliter == null) {
            spliter = ",";
        }
        if (st != null && st.size() > 0) {
            int c = 0;
            for (String s : st) {
                sb.append(s);
                c ++;
                if (c < st.size()) {
                    sb.append(spliter);
                }
            }
        }
        return sb.toString();
    }

    public static String mask(String sourceStr, int fromIndex, int toIndex, char val) {
        char[] sourceChars = sourceStr.toCharArray();
        Arrays.fill(sourceChars, fromIndex, toIndex, val);
        return new String(sourceChars);
    }

    public static String valueOf(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    public static String valueOf(Object o, String deftVal) {
        return o == null ? deftVal : String.valueOf(o);
    }

    public static String formateAmt(String amt) {
        if (StringUtil.isEmpty(amt)) {
            return "0.00";
        } else {
            BigDecimal bd = new BigDecimal(amt);
            return bd.movePointLeft(2).toString();
        }
    }

    public static String transferAmt(String amt) {
        if (StringUtil.isEmpty(amt)) {
            return "0";
        } else {
            BigDecimal bd = new BigDecimal(amt);
            return String.valueOf(bd.movePointRight(2));
        }
    }

    /**
     * 根据正则表达式校验字符串
     * @param val
     * @param reg
     * @param caseSensitive
     * @return
     */
    public static boolean validateReg(String val, String reg, boolean caseSensitive) {
        Pattern pattern = null;
        try {
            if (caseSensitive) {
                pattern = Pattern.compile(reg);
            } else {
                pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
            }
            Matcher matcher = pattern.matcher(val);
            return matcher.matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 展示二维码信息  前2位***后3位
     * @param priAcctNo
     * @return
     */
    public static String maskPriAcctNo(String priAcctNo) {
        if (StringUtil.isBlank(priAcctNo)) {
            return "";
        }
        if (priAcctNo.length() > 8) {
            return mask(priAcctNo, 4, priAcctNo.length() - 4, '*');
        }
        return priAcctNo;
    }

    public static String trimStr(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 判断是否是合法的月份字符串01-12
     * @param mon
     * @return
     */
    public static boolean isValidMonStr(String mon) {
        for (String m : mons) {
            if (m.equals(mon)) {
                return true;
            }
        }
        return false;
    }

    public static String joinUrlParaMap(Map<String, String> map) {
        return joinMapURLEncoderValue(map, '&', "UTF-8");
    }

    public static String joinMapURLEncoderValue(Map<String, String> map, char connector, String encoding) {
        return joinMapURLEncoderValue(map, connector, true, encoding);
    }

    public static String joinMapURLEncoderValue(Map<String, String> map, char connector, boolean appendKey4EmptyValue,
                                                String encoding) {
        StringBuilder b = new StringBuilder();
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    if (entry.getValue() == null) {
                        if (appendKey4EmptyValue) {
                            b.append(URLEncoder.encode(entry.getKey(), encoding));
                            b.append('=');
                            b.append(connector);
                        }
                    } else {
                        b.append(URLEncoder.encode(entry.getKey(), encoding));
                        b.append('=');
                        b.append(URLEncoder.encode(entry.getValue(), encoding));
                        b.append(connector);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            b.deleteCharAt(b.length() - 1);
        }
        return b.toString();
    }

    /**
     * 检查指定的字符串列表是否不为空。
     */
    public static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values == null || values.length == 0) {
            result = false;
        } else {
            for (String value : values) {
                result &= !isEmpty(value);
            }
        }
        return result;
    }

    /**
     * 检查指定的字符串是否为空。
     * <ul>
     * <li>SysUtils.isEmpty(null) = true</li>
     * <li>SysUtils.isEmpty("") = true</li>
     * <li>SysUtils.isEmpty("   ") = true</li>
     * <li>SysUtils.isEmpty("abc") = false</li>
     * </ul>
     *
     * @param value 待检查的字符串
     * @return true/false
     */
    public static boolean isEmpty(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(value.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static String formatDouble(double d) {
        return DEFAULT_DECIMAL_FORMATTER.format(d);
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }
}
