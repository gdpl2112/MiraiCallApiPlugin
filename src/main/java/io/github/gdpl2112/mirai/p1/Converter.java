package io.github.gdpl2112.mirai.p1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.kloping.number.NumberUtils;
import org.jsoup.nodes.Document;

/**
 * @author github.kloping
 */
public class Converter {
    public static final String QID = "$qid";
    public static final String QID0 = "\\$qid";

    public static final String GID = "$gid";
    public static final String GID0 = "\\$gid";

    public static final String CHAR0 = "$%s";

    public static final String ALL = "$all";

    public static final String PAR_URL = "$url";

    public static final String PAR_NUMBER = "$number";

    public static final String PAR_NUMBER0 = "\\$number";

    public static String filterId(String url, long gid, long qid, String... args) {
        if (url == null) return url;
        if (url.contains(QID)) {
            url = url.replaceAll(QID0, String.valueOf(qid));
        }
        if (url.contains(GID)) {
            url = url.replaceAll(GID0, String.valueOf(gid));
        }
        if (url.contains(PAR_NUMBER)) {
            StringBuilder nums = new StringBuilder();
            for (String arg : args) {
                nums.append(NumberUtils.findNumberFromString(arg));
            }
            url = url.replaceAll(PAR_NUMBER0, String.valueOf(nums.toString()));
        }
        return url;
    }

    public static Object get(Document t1, String t0) throws Exception {
        if (t0.equals(ALL)) return t1.body().text();
        if (t0.equals(PAR_URL)) return t1.location();
        return get0(t1.body().text(), t0);
    }

    public static Object get0(String t1, String t0) throws Exception {
        JSON j0 = (JSON) JSON.parse(t1);
        t0 = t0.trim();
        String s0 = t0.trim().split("\\.")[0].trim();
        Object o = null;
        if (s0.matches("\\[\\d+]")) {
            Integer st = Integer.parseInt(s0.substring(1, s0.length() - 1));
            JSONArray arr = (JSONArray) j0;
            o = arr.get(st);
            int len = 4;
            if (t0.length() >= len) t0 = t0.substring(len);
            else t0 = t0.substring(len - 1);
        } else if (s0.matches(".*?\\[\\d+]")) {
            int i = s0.indexOf("[");
            int i1 = s0.indexOf("]");
            String st0 = s0.substring(0, i);
            Integer st = Integer.parseInt(s0.substring(i + 1, s0.length() - 1));
            JSONObject jo = (JSONObject) j0;
            o = jo.getJSONArray(st0).get(st);
            int len = 4 + st0.length();
            if (t0.length() >= len) t0 = t0.substring(len);
            else t0 = t0.substring(len - 1);
        } else {
            JSONObject jo = (JSONObject) j0;
            o = jo.get(s0);
            int len = s0.length() + 1;
            if (t0.length() >= len) t0 = t0.substring(len);
            else t0 = t0.substring(len - 1);
        }
        if (t0.length() > 0) {
            return get0(JSON.toJSONString(o), t0);
        } else {
            return o;
        }
    }

}
