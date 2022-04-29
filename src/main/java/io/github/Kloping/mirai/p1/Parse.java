package io.github.Kloping.mirai.p1;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author github.kloping
 */
public class Parse {
    private static final Pattern PATTER_FACE = Pattern.compile("(<Face:\\d+>|\\[Face:\\d+])");
    private static final Pattern PATTER_PIC = Pattern.compile("(<Pic:[^>^]+?>|\\[Pic:[^>^]+?])");
    private static final Pattern PATTER_URL = Pattern.compile("<Url:[^>^]+>");
    private static final Pattern PATTER_AT = Pattern.compile("\\[At:.+?]|<At:.+?>");
    private static final Pattern PATTER_VOICE = Pattern.compile("\\[Voice:.+?]|<Audio:.+?>");
    public static final Pattern[] PATTERNS = {PATTER_FACE, PATTER_PIC, PATTER_URL, PATTER_AT, PATTER_VOICE};

    public static List<Object> aStart(String line) {
        List<String> list = new ArrayList<>();
        List<Object> olist = new ArrayList<>();
        a1b2c3(list, line);
        for (String s : list) {
            int i = line.indexOf(s);
            if (i > 0) {
                olist.add(line.substring(0, i));
            }
            olist.add(s);
            line = line.substring(i + s.length());
        }
        if (!line.isEmpty())
            olist.add(line);
        return olist;
    }

    public static void a1b2c3(List<String> list, String line) {
        if (list == null || line == null || line.isEmpty()) return;
        Map<Integer, String> nm = getNearestOne(line, PATTER_PIC, PATTER_AT, PATTER_FACE, PATTER_URL);
        if (nm.isEmpty()) {
            list.add(line);
            return;
        }
        int n = nm.keySet().iterator().next();
        String v = nm.get(n);
        String[] ss = new String[2];
        ss[0] = line.substring(0, line.indexOf(v));
        ss[1] = line.substring(line.indexOf(v) + v.length(), line.length());
        if (!ss[0].isEmpty()) {
            list.add(ss[0]);
            line = line.substring(ss[0].length());
        }
        line = ss[1];
        list.add(v);
        a1b2c3(list, line);
        return;
    }

    private static final Set<Character> TS = new HashSet<>(Arrays.asList(
            new Character[]{'*', '.', '?', '+', '$', '^', '[', ']', '(', ')', '{', '}', '|', '\\', '/'})
    );

    public static Map<Integer, String> getNearestOne(final String line, Pattern... patterns) {
        try {
            Map<Integer, String> map = new LinkedHashMap<>();
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String l1 = matcher.group();
                    int i1 = line.indexOf(l1);
                    map.put(i1, l1);
                }
            }
            Map<Integer, String> result1 = new LinkedHashMap<>();
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(x -> result1.put(x.getKey(), x.getValue()));
            return result1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Long> parseToLongList(String s0) {
        if (s0 == null) return new ArrayList<>();
        List<Long> set = new ArrayList<>();
        for (String s : s0.split(",")) {
            if (s.trim().isEmpty()) continue;
            set.add(Long.parseLong(s.trim()));
        }
        return set;
    }
}
