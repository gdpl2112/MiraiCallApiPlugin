package io.github.Kloping.mirai.p1.rest;

import io.github.Kloping.mirai.p1.CallTemplate;
import io.github.kloping.initialize.FileInitializeValue;
import io.github.kloping.little_web.annotations.RequestMethod;
import io.github.kloping.little_web.annotations.RequestParm;
import io.github.kloping.little_web.annotations.WebRestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

import static io.github.Kloping.mirai.p1.CallApiPlugin.CONF_FILE;
import static io.github.Kloping.mirai.p1.CallApiPlugin.conf;

/**
 * @author github.kloping
 */
@WebRestController
public class RestController0 {

    private boolean verify(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return false;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("key".equals(cookie.getName())) {
                if (cookie.getValue().equals(conf.getPasswd())) {
                    return true;
                }
            }
        }
        return false;
    }

    @RequestMethod("/")
    public void index(@RequestParm("key") String key, HttpServletResponse response) throws IOException {
        if (key != null && key.equals(conf.getPasswd())) {
            Cookie cookie = new Cookie("key", key);
            response.addCookie(cookie);
            response.sendRedirect("/index.html");
        } else {
            response.sendRedirect("https://www.baidu.com");
        }
    }

    @RequestMethod("/get_data")
    public Object getAll(HttpServletRequest request) {
        if (!verify(request)) return null;
        return conf.getTemplates();
    }

    @RequestMethod("/delete")
    public Object delete(@RequestParm("touch") String touch, HttpServletRequest request) {
        if (!verify(request)) return null;
        Iterator<CallTemplate> iterator = conf.getTemplates().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().touch.equals(touch)) {
                iterator.remove();
                conf = FileInitializeValue.putValues(CONF_FILE.getAbsolutePath(), conf, true);
                return getAll(request);
            }
        }
        return getAll(request);
    }

    @RequestMethod("/append")
    public Object append(
            @RequestParm("touch") String touch,
            @RequestParm("out") String out,
            @RequestParm("outArgs") String outArgs,
            @RequestParm("url") String url,
            @RequestParm("proxy") String proxy, HttpServletRequest request) {
        if (!verify(request)) return null;
        CallTemplate template = new CallTemplate();
        template.setOut(out);
        template.setOutArgs(outArgs.split(","));
        template.setUrl(url);
        template.setTouch(touch);
        if (proxy != null && !proxy.isEmpty()) {
            template.setProxyIp(proxy.split(":")[0]);
            template.setProxyPort(Integer.valueOf(proxy.split(":")[1]));
        }
        conf.getTemplates().add(template);
        conf = FileInitializeValue.putValues(CONF_FILE.getAbsolutePath(), conf, true);
        return getAll(request);
    }
}
