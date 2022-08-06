package io.github.gdpl2112.mirai.p1;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.StarterObjectApplication;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import io.github.kloping.little_web.conf.TomcatConfig;
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

import static io.github.gdpl2112.mirai.p1.CallApiPlugin.conf;
import static io.github.gdpl2112.mirai.p1.Converter.*;
import static io.github.gdpl2112.mirai.p1.Parse.getMessageFromString;

/**
 * @author github.kloping
 */
@CommentScan(path = "io.github.gdpl2112.mirai.p1.rest")
public class Worker {
    public static boolean inited = false;

    static void init() {
        if (inited) return;
        inited = true;
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
        TomcatConfig config = new TomcatConfig();
        config.setName("callApi-web");
        config.setPort(conf.getPort());
        StarterObjectApplication application = new StarterObjectApplication();
        application.SCAN_LOADER = Worker.class.getClassLoader();
        application.PRE_SCAN_RUNNABLE.add(() -> {
            application.INSTANCE.getContextManager().append(config);
            application.INSTANCE.getContextManager().append("callApi-servlet0", "servletName");
        });
        StarterApplication.logger = application.logger;
        application.run0(Worker.class);
        application.logger.info("服务启动成功 请访问 http://localhost:" + conf.getPort() + "/?key=" + conf.getPasswd());
    }

    public static Message call(String text, long gid, long qid) {
        try {
            Conf conf = CallApiPlugin.conf;
            String[] ss = text.split(conf.getSplitChar());
            String first = ss[0];
            for (CallTemplate template : conf.getTemplates()) {
                if (template.touch.equals(first)) {
                    if (!template.sw) continue;
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
                if (o0 != null) {
                    end = end.replace(String.format(CHAR0, i++), o0.toString());
                }
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

    public static Document doc(long gid, long qid, CallTemplate template, String... args) {
        int i = 1;
        String url = template.url;
        for (String arg : args) {
            url = url.replace(String.format(CHAR0, i++), arg);
        }
        url = filterId(url, gid, qid, args);
        try {
            Connection connection = org.jsoup.Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true)
                    .header("Host", new URL(url).getHost())
                    .header("accept-encoding", "gzip, deflate, br")
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36 Edg/100.0.1185.50"
                    );
            connection.timeout(600000);
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
