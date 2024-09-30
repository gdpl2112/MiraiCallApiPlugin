package io.github.gdpl2112.mirai.p1.services;

import com.alibaba.fastjson.JSON;
import io.github.kloping.arr.ArrSerializer;
import io.github.kloping.io.ReadUtils;
import io.github.kloping.url.UrlUtils;
import net.mamoe.mirai.Bot;
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
public class DgSerializer {
    private static final Pattern PATTER_FACE = Pattern.compile("<face:\\d+>");
    private static final Pattern PATTER_PIC = Pattern.compile("<pic:[^>^]+?>");
    private static final Pattern PATTER_URL = Pattern.compile("<url:[^>^]+>");
    private static final Pattern PATTER_AT = Pattern.compile("<at:[\\d+|?]+>");
    private static final Pattern PATTER_MUSIC = Pattern.compile("<music:\\d+>");
    private static final Pattern PATTER_VOICE = Pattern.compile("<audio:.+>");
    private static final Pattern PATTER_MIRAI_FACE = Pattern.compile("\\[mirai:face:.*?]");
    private static final Pattern PATTER_MIRAI_IMAGE = Pattern.compile("\\[mirai:image:.*?]");

    public static final Pattern[] PATTERNS = {PATTER_FACE, PATTER_PIC, PATTER_URL, PATTER_AT, PATTER_VOICE, PATTER_MUSIC, PATTER_MIRAI_FACE, PATTER_MIRAI_IMAGE};

    private static final String BASE64 = "base64,";

    public static final Map<Integer, MarketFace> MARKET_FACE_MAP = new HashMap<>();

    public static MessageChain stringDeserializeToMessageChain(String str, Bot bot, Contact contact) {
        if (str == null || str.isEmpty() || bot == null) return null;
        MessageChainBuilder builder = new MessageChainBuilder();
        goToFormat(str, builder, bot, contact);
        MessageChain message = builder.build();
        return message;
    }

    private static List<Object> goToFormat(String sb, MessageChainBuilder builder, Bot bot, Contact contact) {
        List<Object> allElements = getAllElements(sb);
        for (Object o : allElements) {
            String str = o.toString();
            boolean k = (str.startsWith("<") || str.startsWith("[")) && !str.matches("\\[.+]请使用最新版手机QQ体验新功能");
            if (k) {
                Message msg = null;
                String ss = str.replaceAll("[<>\\[\\]]", "");
                int i1 = ss.indexOf(":");
                String s1 = ss.substring(0, i1);
                String s2 = ss.substring(i1 + 1);
                switch (s1.toLowerCase()) {
                    case "pic":
                        msg = createImage(contact, bot, s2);
                        break;
                    case "face":
                        msg = new Face(Integer.parseInt(s2));
                        break;
                    case "at":
                        long tid = -1L;
                        if (s2.contains("?")) tid = contact.getId();
                        else tid = Long.parseLong(s2);
                        msg = new At(tid);
                        break;
                    case "voice":
                    case "audio":
                        msg = createVoiceMessageInGroup(s2, bot.getId(), contact);
                        break;
                    case "music":
                        msg = createMusic(bot, s2);
                        break;
                    case "marketface":
                        msg = MARKET_FACE_MAP.get(Integer.parseInt(s2));
                        break;
                    case "mirai":
                        String type = s2.substring(0, s2.indexOf(":"));
                        String c0 = s2.substring(s2.indexOf(":") + 1, s2.length());
                        if (type.equals("face"))
                            msg = new Face(Integer.parseInt(c0));
                        else if (type.equals("image"))
                            msg = createImage(contact, bot, c0);
                        break;
                    default:
                        msg = new PlainText(s2);
                        break;
                }
                if (msg != null) builder.append(msg);
            } else {
                builder.append(str);
            }
        }
        return allElements;
    }

    public static List<Object> getAllElements(String line) {
        List<String> list = new ArrayList<>();
        List<Object> olist = new ArrayList<>();
        algorithmFill(list, line);
        for (String s : list) {
            int i = line.indexOf(s);
            if (i > 0) {
                olist.add(line.substring(0, i));
            }
            olist.add(s);
            line = line.substring(i + s.length());
        }
        if (!line.isEmpty()) olist.add(line);
        return olist;
    }

    public static void algorithmFill(List<String> list, String line) {
        if (list == null || line == null || line.isEmpty()) return;
        Map<Integer, String> nm = getNearestOne(line, PATTERNS);
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
        algorithmFill(list, line);
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

    private static Message createMusic(Bot contact, String vals) {
        String[] ss = vals.split(",");
        MusicKind kind = MusicKind.valueOf(ss[0]);
        MusicShare share = new MusicShare(kind, ss[1], ss[2], ss[3], ss[4], ss[5]);
        return share;
    }

    public static Message createImage(Contact contact, Bot bot, String path) {
        Message image = null;
        try {
            if (path.startsWith("http")) {
                image = Contact.uploadImage(bot.getAsFriend(), new ByteArrayInputStream(ReadUtils.readAll(new URL(path).openStream())));
            } else if (path.startsWith("{")) {
                image = Image.fromId(path);
            } else if (path.contains(BASE64)) {
                image = Contact.uploadImage(bot.getAsFriend(), new ByteArrayInputStream(getBase64Data(path)));
            } else if (path.startsWith("[") && path.endsWith("]")) {
                image = createForwardMessageByPic(contact, bot, (String[]) JSON.parseArray(path).toArray(new String[0]));
            } else {
                image = Contact.uploadImage(bot.getAsFriend(), new File(path));
            }
        } catch (Exception e) {
            System.err.println(path + "加载失败");
            e.printStackTrace();
        }
        if (image != null) return image;
        else return null;
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
            } else return new PlainText(url);
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
    }

    public static Message createForwardMessageByPic(Contact contact, Bot bot, String[] picUrl) {
        ForwardMessageBuilder builder = new ForwardMessageBuilder(contact);
        for (String s : picUrl) builder.add(bot.getId(), bot.getBot().getNick(), createImage(contact, bot, s));
        return builder.build();
    }

    public static final ArrSerializer ARR_SERIALIZER = new ArrSerializer();

    static {
        ARR_SERIALIZER.add(new ArrSerializer.Rule<Image>(Image.class) {
            @Override
            public String serializer(Image o) {
                return String.format("<pic:%s>", o.getImageId());
            }
        });
        ARR_SERIALIZER.add(new ArrSerializer.Rule<At>(At.class) {
            @Override
            public String serializer(At o) {
                return String.format("<at:%s>", o.getTarget());
            }
        });
        ARR_SERIALIZER.add(new ArrSerializer.Rule<Face>(Face.class) {
            @Override
            public String serializer(Face o) {
                return String.format("<face:%s>", o.getId());
            }
        });
        ARR_SERIALIZER.add(new ArrSerializer.Rule<PlainText>(PlainText.class) {
            @Override
            public String serializer(PlainText o) {
                String touch = o.getContent();
                String regx = "<.*?>";
                Pattern pattern = Pattern.compile(regx);
                Matcher matcher = pattern.matcher(touch);
                while (matcher.find()) {
                    touch = touch.replace(matcher.group(), "\\" + matcher.group());
                }
                return touch;
            }
        });
        ARR_SERIALIZER.add(new ArrSerializer.Rule<Audio>(Audio.class) {
            @Override
            public String serializer(Audio o) {
                return String.format("<audio:%s>", o.getFilename());
            }
        });
        ARR_SERIALIZER.add(new ArrSerializer.Rule<MusicShare>(MusicShare.class) {
            @Override
            public String serializer(MusicShare o) {
                return String.format("<music:%s>", o.getMusicUrl());
            }
        });
        ARR_SERIALIZER.add(new ArrSerializer.Rule<MarketFace>(MarketFace.class) {
            @Override
            public String serializer(MarketFace o) {
                MARKET_FACE_MAP.put(o.getId(), o);
                return String.format("<marketface:%s>", o.getId());
            }
        });
//        ARR_SERIALIZER.add(new ArrSerializer.Rule<QuoteReply>(QuoteReply.class) {
//            @Override
//            public String serializer(QuoteReply o) {
//                return String.format("<qr:%s>", AllMessage.latest(0, o.getSource().getInternalIds()));
//            }
//        });
        ARR_SERIALIZER.setMode(1);
    }

    public static String messageChainSerializeToString(MessageChain chain) {
        return ARR_SERIALIZER.serializer(chain);
    }
}
