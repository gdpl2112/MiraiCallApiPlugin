package io.github.Kloping.mirai.p1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.kloping.url.UrlUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.Kloping.mirai.p1.CallApiPlugin.conf;
import static io.github.Kloping.mirai.p1.Parse.aStart;

/**
 * @author github.kloping
 */
public class Worker {
    public static final String QID = "$qid";
    public static final String QID0 = "\\$qid";
    public static final String GID = "$gid";
    public static final String GID0 = "\\$gid";
    public static final String CHAR0 = "$%s";
    public static final String ALL = "$all";

    private static final Map<Integer, Face> FACES = new ConcurrentHashMap<>();
    private static final Map<Long, At> ATS = new ConcurrentHashMap<>();
    private static final Map<String, Image> HIST_IMAGES = new ConcurrentHashMap<>();

    static {
        try {
            SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, new java.security.SecureRandom());

            HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslsession) {
                    System.out.println("WARNING: Hostname is not matched for cert.");
                    return true;
                }
            };

            HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static Message call(String text, long gid, long qid) {
        try {
            Conf conf = CallApiPlugin.conf;
            String[] ss = text.split(conf.getSplitChar());
            String first = ss[0];
            for (CallTemplate template : conf.getTemplates()) {
                if (template.touch.equals(first)) {
                    String[] ss0 = new String[ss.length - 1];
                    System.arraycopy(ss, 1, ss0, 0, ss0.length);
                    Document document = doc(gid, qid, template, ss0);
                    if (document == null) return null;
                    return parse(document, template, Bot.getInstances().get(0).getAsFriend(), gid, qid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Message parse(Document document, CallTemplate template, Contact contact, long gid, long qid) {
        String end = template.out;
        try {
            int i = 1;
            for (String outArg : template.outArgs) {
                Object o0 = get(document.body().text(), outArg);
                end = end.replace(String.format(CHAR0, i++), o0.toString());
            }
            end = filterId(end, gid, qid);
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                e.printStackTrace();
            }
            if (template.err != null && !template.err.isEmpty()) {
                end = template.err;
            } else {
                end = "调用失败";
            }
        }
        return getMessageFromString(end, contact);
    }

    private static Object get(String t1, String t0) throws Exception {
        if (t0.equals(ALL)) return t1;
        JSON j0 = (JSON) JSON.parse(t1);
        String s0 = t0.split("\\.")[0];
        Object o = null;
        if (s0.matches("\\[\\d]")) {
            Integer st = Integer.parseInt(s0.substring(1, s0.length() - 1));
            JSONArray arr = (JSONArray) j0;
            o = arr.get(st);
            int len = 4;
            if (t0.length() >= len) t0 = t0.substring(len);
            else t0 = t0.substring(len - 1);
        } else if (s0.matches(".*?\\[\\d]")) {
            int i = s0.indexOf("[");
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
            return get(JSON.toJSONString(o), t0);
        } else {
            return o;
        }
    }

    public static Document doc(long gid, long qid, CallTemplate template, String... args) {
        int i = 1;
        String url = template.url;
        for (String arg : args) {
            url = url.replace(String.format(CHAR0, i++), arg);
        }
        url = filterId(url, gid, qid);
        try {
            Connection connection = org.jsoup.Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true)
                    .header("Host", new URL(url).getHost())
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36 Edg/100.0.1185.50"
                    );
            if (template.getProxyIp() != null && template.getProxyPort() != null && !template.getProxyIp().isEmpty()) {
                connection.proxy(template.getProxyIp(), template.getProxyPort());
            } else if (conf.getProxyIp() != null && conf.getProxyPort() != null && !conf.getProxyIp().isEmpty()) {
                connection.proxy(conf.getProxyIp(), conf.getProxyPort());
            }
            return connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String filterId(String url, long gid, long qid) {
        if (url == null) return url;
        if (url.contains(QID)) {
            url = url.replaceAll(QID0, String.valueOf(qid));
        }
        if (url.contains(GID)) {
            url = url.replaceAll(GID0, String.valueOf(gid));
        }
        return url;
    }

    public static MessageChain getMessageFromString(String str, Contact group) {
        if (str == null || str.isEmpty() || group == null) return null;
        MessageChainBuilder builder = new MessageChainBuilder();
        append(str, builder, group);
        MessageChain message = builder.build();
        return message;
    }

    private static List<Object> append(String sb, MessageChainBuilder builder, Contact contact) {
        List<Object> lls = aStart(sb);
        for (Object o : lls) {
            String str = o.toString();
            boolean k = (str.startsWith("<") || str.startsWith("[")) && !str.matches("\\[.+]请使用最新版手机QQ体验新功能");
            if (k) {
                String ss = str.replace("<", "").replace(">", "")
                        .replace("[", "").replace("]", "");
                int i1 = ss.indexOf(":");
                String s1 = ss.substring(0, i1);
                String s2 = ss.substring(i1 + 1);
                switch (s1) {
                    case "Pic":
                        builder.append(createImage(contact, s2));
                        break;
                    case "Face":
                        builder.append(getFace(Integer.parseInt(s2)));
                        break;
                    case "At":
                        builder.append(getAt(Long.parseLong(s2)));
                        break;
                    case "Voice":
                    case "Audio":
                        builder.append(createVoiceMessageInGroup(s2, contact.getId(), contact));
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

    public static Image createImage(Contact group, String path) {
        Image image = null;
        try {
            if (HIST_IMAGES.containsKey(path)) {
                image = HIST_IMAGES.get(path);
            } else if (path.startsWith("http")) {
                image = Contact.uploadImage(group, new URL(path).openStream());
            } else if (path.startsWith("{")) {
                image = Image.fromId(path);
            } else if (path.contains("base64,")) {
                image = Contact.uploadImage(group, new ByteArrayInputStream(getBase64Data(path)));
            } else {
                image = Contact.uploadImage(group, new File(path));
            }
        } catch (Exception e) {
            System.err.println(path + "加载失败");
            e.printStackTrace();
        }
        if (image != null) {
            HIST_IMAGES.put(path, image);
            if (HIST_IMAGES.size() >= 100) {
                HIST_IMAGES.clear();
            }
        }
        return image;
    }

    private static final String BASE64 = "base64,";

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
}
