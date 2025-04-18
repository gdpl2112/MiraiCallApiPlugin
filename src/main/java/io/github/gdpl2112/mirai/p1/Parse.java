package io.github.gdpl2112.mirai.p1;

import com.alibaba.fastjson.JSON;
import io.github.kloping.io.ReadUtils;
import io.github.kloping.url.UrlUtils;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author github.kloping
 */
public class Parse {
    private static final Pattern PATTER_FACE = Pattern.compile("<Face:\\d+>");
    private static final Pattern PATTER_PIC = Pattern.compile("<Pic:[^>^]+?>|<pic:[^>^]+?>");
    private static final Pattern PATTER_URL = Pattern.compile("<Url:[^>^]+>|<url:[^>^]+?>");
    private static final Pattern PATTER_AT = Pattern.compile("<At:\\d+>|<at:\\d+>");
    private static final Pattern PATTER_MUSIC = Pattern.compile("<Music:\\d+>|<music:\\d+>");
    private static final Pattern PATTER_VOICE = Pattern.compile("<Audio:.+>");
    public static final Pattern[] PATTERNS = {PATTER_FACE, PATTER_PIC, PATTER_URL, PATTER_AT, PATTER_VOICE, PATTER_MUSIC};

    private static final String BASE64 = "base64,";

    private static final Map<Integer, Face> FACES = new HashMap<>();
    private static final Map<Long, At> ATS = new HashMap<>();

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
        Map<Integer, String> nm = getNearestOne(line, PATTER_FACE, PATTER_PIC, PATTER_URL, PATTER_AT, PATTER_VOICE, PATTER_MUSIC);
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

    public static MessageChain getMessageFromString(String str, Contact contact) {
        if (str == null || str.isEmpty() || contact == null) return null;
        MessageChainBuilder builder = new MessageChainBuilder();
        append(str, builder, contact);
        MessageChain message = builder.build();
        return message;
    }

    private static List<Object> append(String sb, MessageChainBuilder builder, Contact contact) {
        List<Object> lls = aStart(sb);
        for (Object o : lls) {
            String str = o.toString();
            boolean k = (str.startsWith("<") || str.startsWith("[")) && !str.matches("\\[.+]请使用最新版手机QQ体验新功能");
            if (k) {
                String ss = str.replace("<", "").replace(">", "");
                int i1 = ss.indexOf(":");
                String s1 = ss.substring(0, i1);
                String s2 = ss.substring(i1 + 1);
                switch (s1) {
                    case "Pic":
                    case "pic":
                        builder.append(createImage(contact, s2));
                        break;
                    case "Face":
                    case "face":
                        builder.append(getFace(Integer.parseInt(s2)));
                        break;
                    case "At":
                    case "at":
                        builder.append(getAt(Long.parseLong(s2)));
                        break;
                    case "Voice":
                    case "Audio":
                        builder.append(createVoiceMessageInGroup(s2, contact.getId(), contact));
                        break;
                    case "Music":
                    case "music":
                        builder.append(createMusic(contact, s2));
                        break;
                    default:
                        break;
                }
            } else {
                builder.append(str);
            }
        }
        return lls;
    }

    private static Message createMusic(Contact contact, String s2) {
        String[] ss = s2.split(",");
        MusicKind kind = MusicKind.valueOf(ss[0]);
        MusicShare share = new MusicShare(kind
                , ss[1]
                , ss[2]
                , ss[3]
                , ss[4]
                , ss[5]
        );
        return share;
    }

    private static Face getFace(int parseInt) {
        if (FACES.containsKey(parseInt)) {
            return FACES.get(parseInt);
        } else {
            Face face = new Face(parseInt);
            FACES.put(parseInt, face);
            return face;
        }
    }

    public static At getAt(long id) {
        if (ATS.containsKey(id)) {
            return ATS.get(id);
        } else {
            At at = new At(id);
            ATS.put(id, at);
            return at;
        }
    }

    public static Message createImage(Contact group, String path) {
        Message image = null;
        try {
            if (path.startsWith("http")) {
                image = Contact.uploadImage(group, new ByteArrayInputStream(ReadUtils.readAll(new URL(path).openStream())));
            } else if (path.startsWith("{")) {
                image = Image.fromId(path);
            } else if (path.contains(BASE64)) {
                image = Contact.uploadImage(group, new ByteArrayInputStream(getBase64Data(path)));
            } else if (path.startsWith("[") && path.endsWith("]")) {
                image = createForwardMessageByPic(group, (String[]) JSON.parseArray(path).toArray(new String[0]));
            } else {
                image = Contact.uploadImage(group, new File(path));
            }
        } catch (Exception e) {
            System.err.println(path + "加载失败");
            e.printStackTrace();
        }
        return image;
    }

    public static byte[] getBase64Data(String base64) {
        int i = base64.indexOf(BASE64);
        String base64Str = base64.substring(i + BASE64.length());
        byte[] bytes = Base64.getDecoder().decode(base64Str);
        return bytes;
    }

    public static Message createVoiceMessageInGroup(String url, long id, Contact contact) {
        ExternalResource resource = null;
        try {
            byte[] bytes = UrlUtils.getBytesFromHttpUrl(url);
            resource = ExternalResource.create(bytes);
            if (contact instanceof Group) {
                return ((Group) contact).uploadAudio(resource);
            } else if (contact instanceof Friend) {
                return ((Friend) contact).uploadAudio(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Message createForwardMessageByPic(Contact contact, String[] picUrl) {
        ForwardMessageBuilder builder = new ForwardMessageBuilder(contact);
        for (String s : picUrl) {
            builder.add(contact.getId(), contact.getBot().getNick(), createImage(contact, s));
        }
        return builder.build();
    }
}
