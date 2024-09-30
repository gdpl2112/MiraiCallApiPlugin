package io.github.gdpl2112.mirai.p1.services.script;

import io.github.gdpl2112.mirai.p1.services.DgSerializer;
import io.github.kloping.map.MapUtils;
import io.github.kloping.url.UrlUtils;
import net.mamoe.mirai.message.data.MessageChain;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

public class BaseScriptUtils implements ScriptUtils {
    private long bid;

    public BaseScriptUtils(long bid) {
        this.bid = bid;
    }

    public static final Map<Long, Map<String, Object>> BID_2_VARIABLES = new HashMap<>();

    @Override
    public String requestGet(String url) {
        return UrlUtils.getStringFromHttpUrl(url);
    }

    @Override
    public String requestPost(String url, String data) {
        try {
            return Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).requestBody(data).post().body().wholeText();
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }
    }

    @Override
    public String serialize(MessageChain chain) {
        return DgSerializer.messageChainSerializeToString(chain);
    }

    @Override
    public Object get(String name) {
        return getValueOrDefault(BID_2_VARIABLES, bid, name, null);
    }

    @Override
    public Object set(String name, Object value) {
        Object ov = getValueOrDefault(BID_2_VARIABLES, bid, name, null);
        MapUtils.append(BID_2_VARIABLES, bid, name, value, HashMap.class);
        return ov;
    }

    @Override
    public Integer clear() {
        int i = 0;
        Map<String, Object> sizeMap = BID_2_VARIABLES.get(bid);
        if (sizeMap != null) {
            i = sizeMap.size();
            sizeMap.clear();
        }
        return i;
    }

    @Override
    public Object del(String name) {
        Map<String, Object> sizeMap = BID_2_VARIABLES.get(bid);
        if (sizeMap != null) {
            Object oa = sizeMap.get(name);
            sizeMap.remove(name);
            return oa;
        }
        return null;
    }

    @Override
    public List<Map.Entry<String, Object>> list() {
        if (BID_2_VARIABLES.containsKey(bid))
            return new LinkedList<>(BID_2_VARIABLES.get(bid).entrySet());
        return new ArrayList<>();
    }

    @Override
    public <T> T newObject(String name, Object... args) {
        try {
            Class cla = Class.forName(name);
            List<Class> list = new ArrayList<>();
            for (Object arg : args) {
                list.add(arg.getClass());
            }
            Constructor constructor = cla.getDeclaredConstructor(list.toArray(new Class[0]));
            return (T) constructor.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static final <T, K1, K2> T getValueOrDefault(Map<K1, Map<K2, T>> map, K1 k1, K2 k2, T def) {
        if (map.containsKey(k1)) {
            Map<K2, T> m2 = map.get(k1);
            if (m2.containsKey(k2)) {
                return m2.get(k2);
            } else {
                m2.put(k2, def);
                map.put(k1, m2);
                return def;
            }
        } else {
            Map<K2, T> m2 = new HashMap<>();
            m2.put(k2, def);
            map.put(k1, m2);
            return def;
        }
    }
}
