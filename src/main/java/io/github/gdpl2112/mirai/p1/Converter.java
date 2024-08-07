package io.github.gdpl2112.mirai.p1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.kloping.io.ReadUtils;
import io.github.kloping.number.NumberUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author github.kloping
 */
public class Converter {
    public static final String QID = "$qid";
    public static final String QID0 = "\\$qid";

    public static final String QNAME = "$qname";
    public static final String QNAME0 = "\\$qname";

    public static final String MNAME = "$mname";
    public static final String MNAME0 = "\\$mname";

    public static final String GNAME = "$gname";
    public static final String GNAME0 = "\\$gname";

    public static final String GID = "$gid";
    public static final String GID0 = "\\$gid";

    public static final String CHAR0 = "\\$%s";

    public static final String ALL = "$all";

    public static final String PAR_URL = "$url";

    public static final String PAR_NUMBER = "$number";
    public static final String PAR_NUMBER0 = "\\$number";

    public static String filterId(String url, Bot bot, long gid, long qid, String... args) {
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
        if (url.contains(QNAME)) {
            Friend friend = bot.getFriend(qid);
            if (friend != null) {
                url = url.replaceAll(QNAME0, friend.getNick());
            } else {
                Member member = bot.getGroup(gid).getMembers().get(qid);
                if (member != null)
                    url = url.replaceAll(QNAME0, member.getNick());
            }
        }
        if (url.contains(MNAME)) {
            Member member = bot.getGroup(gid).getMembers().get(qid);
            if (member != null)
                url = url.replaceAll(MNAME0, member.getNameCard());
        }
        if (url.contains(GNAME)) {
            Group group = bot.getGroup(gid);
            if (group != null)
                url = url.replaceAll(GNAME0, group.getName());
        }
        return url;
    }

    public static Object get(Connection t1, String t0, AtomicReference<Document> doc0) throws Exception {
        if (t0.equals(ALL)) {
            return ReadUtils.readAll(t1.execute().bodyStream(), "utf-8");
        }
        if (doc0.get() == null) {
            doc0.set(t1.get());
        }
        if (t0.equals(PAR_URL)) {
            return doc0.get().location();
        }
        return get0(doc0.get().body().text(), t0);
    }

    /**
     * get from json
     *
     * @param t1 json
     * @param t0 表达式
     * @return
     * @throws Exception
     */
    public static Object get0(String t1, String t0) throws Exception {
        JSON j0 = (JSON) JSON.parse(t1);
        t0 = t0.trim();
        String s0 = null;
        for (String s : t0.trim().split("\\.")) {
            if (!s.isEmpty()) {
                s0 = s;
                break;
            }
        }
        Object o = null;
        if (s0.matches("\\[\\d*]")) {
            JSONArray arr = (JSONArray) j0;
            String sts = s0.substring(1, s0.length() - 1);
            if (sts.isEmpty()) {
                o = arr;
                t0 = t0.replaceFirst("\\[]", "");
            } else {
                Integer st = Integer.parseInt(sts);
                o = arr.get(st);
                int len = 4;
                if (t0.length() >= len) t0 = t0.substring(len);
                else t0 = t0.substring(len - 1);
            }
        } else if (s0.matches(".*?\\[\\d+]")) {
            int i = s0.indexOf("[");
            int i1 = s0.indexOf("]");
            String st0 = s0.substring(0, i);
            Integer st = Integer.parseInt(s0.substring(i + 1, s0.length() - 1));
            JSONObject jo = (JSONObject) j0;
            o = jo.getJSONArray(st0).get(st);
            if (t0.length() > s0.length()) t0 = t0.substring(s0.length());
            else t0 = null;
        } else {
            JSONObject jo = (JSONObject) j0;
            o = jo.get(s0);
            int len = s0.length() + 1;
            if (t0.length() >= len) t0 = t0.substring(len);
            else t0 = t0.substring(len - 1);
        }
        if (t0 != null && t0.length() > 0) {
            return get0(JSON.toJSONString(o), t0);
        } else {
            return o;
        }
    }

}
