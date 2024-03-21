package io.github.gdpl2112.mirai.p1;

import com.alibaba.fastjson.JSON;
import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.StarterObjectApplication;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import io.github.kloping.little_web.conf.TomcatConfig;
import io.github.kloping.reg.MatcherUtils;
import net.mamoe.mirai.Bot;
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
import java.util.concurrent.atomic.AtomicReference;

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
        if (conf.getIgnoreVerify()){
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
        startWeb();
    }

    private static void startWeb() {
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

    public static Message call(String text, long gid, long qid, Bot bot) {
        try {
            Conf conf = CallApiPlugin.conf;
            String[] ss = text.split(conf.getSplitChar());
            String first = ss[0];
            for (CallTemplate template : conf.getTemplates()) {
                if (template.touch.equals(first)) {
                    if (!template.sw) continue;
                    if (!enable(template, gid, qid)) continue;
                    String[] ss0 = new String[ss.length - 1];
                    System.arraycopy(ss, 1, ss0, 0, ss0.length);
                    Connection connection = doc(bot, gid, qid, template, ss0);
                    if (connection == null) return null;
                    return parse(connection, template, bot, gid, qid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean enable(CallTemplate template, long gid, long qid) {
        if (gid == qid) {
            return ManagerConf.INSTANCE.getStateByTouchAndIdDefault(template.touch, "f" + qid, true);
        } else {
            return ManagerConf.INSTANCE.getStateByTouchAndIdDefault(template.touch, "g" + gid, true);
        }
    }

    private static Message parse(Connection connection, CallTemplate template, Bot bot, long gid, long qid) {
        Message message = null;
        String end = template.out;
        try {
            int i = 1;
            AtomicReference<Document> doc0 = new AtomicReference<>();
            for (String outArg : template.outArgs) {
                Object o0 = get(connection, outArg, doc0);
                if (o0 != null) {
                    String o1 = o0.toString();
                    try {
                        JSON json = (JSON) JSON.parse(o1);
                    } catch (Exception e) {
                        o1 = o1.replaceAll(",", ";");
                    }
                    end = end.replaceFirst(String.format(CHAR0, i++), o1);
                }
            }
            end = filterId(end, bot, gid, qid);
            end = filterCall(end, template);
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
            message = getMessageFromString(end, bot.getAsFriend());
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

    public static Connection doc(Bot bot, long gid, long qid, CallTemplate template, String... args) throws Exception {
        int i = 1;
        String url = template.url;
        for (String arg : args) {
            url = url.replaceFirst(String.format(CHAR0, i++), arg);
        }
        url = filterId(url, bot, gid, qid, args);
        url = filterCall(url, template);
        try {
            return getConnection(url, template);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String filterCall(String url, CallTemplate template) throws Exception {
        for (String s : MatcherUtils.matcherAll(url, "\\$call.?(.+)")) {
            String ex = s.substring(s.indexOf("call") + 4, s.indexOf("("));
            String u0 = s.substring(s.indexOf("(") + 1, s.lastIndexOf(")"));
            String body = getConnection(u0, template).get().body().text();
            if (ex.isEmpty()) url = url.replace(s, body);
            else url = url.replace(s, get0(body, ex).toString());
        }
        return url;
    }

    public static Connection getConnection(String url, CallTemplate template) throws Exception {
        Connection connection = org.jsoup.Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).header("Host", new URL(url).getHost()).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36 Edg/100.0.1185.50");
        connection.timeout(60000);
        if (template.getProxyIp() != null && template.getProxyPort() != null && !template.getProxyIp().isEmpty()) {
            connection.proxy(template.getProxyIp(), template.getProxyPort());
        } else if (conf.getProxyIp() != null && conf.getProxyPort() != null && !conf.getProxyIp().isEmpty()) {
            connection.proxy(conf.getProxyIp(), conf.getProxyPort());
        }
        return connection;
    }
}
