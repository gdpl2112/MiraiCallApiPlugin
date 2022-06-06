package io.github.Kloping.mirai.p1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static io.github.Kloping.mirai.p1.CallApiPlugin.conf;
import static io.github.Kloping.mirai.p1.Parse.getMessageFromString;

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
    public static final String PAR_URL = "$url";
    public static final String PAR_URL0 = "\\$url";

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
        Message message = null;
        String end = template.out;
        try {
            int i = 1;
            for (String outArg : template.outArgs) {
                Object o0 = get(document, outArg);
                if (o0 != null)
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
        try {
            message = getMessageFromString(end, contact);
        } catch (Exception e) {
            e.printStackTrace();
            if (template.err != null && !template.err.isEmpty()) {
                end = template.err;
            } else {
                end = "调用失败";
            }
            message = new PlainText(end);
        }
        return message;
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

    private static Object get(Document t1, String t0) throws Exception {
        if (t0.equals(ALL)) return t1.body().text();
        if (t0.equals(PAR_URL)) return t1.location();
        return get0(t1.body().text(), t0);
    }

    private static Object get0(String t1, String t0) throws Exception {
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
            return get0(JSON.toJSONString(o), t0);
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

}
