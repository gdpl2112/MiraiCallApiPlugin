package io.github.gdpl2112.mirai.p1.rest;

import io.github.gdpl2112.mirai.p1.CallApiPlugin;
import io.github.gdpl2112.mirai.p1.CallTemplate;
import io.github.kloping.initialize.FileInitializeValue;
import io.github.kloping.little_web.annotations.RequestMethod;
import io.github.kloping.little_web.annotations.RequestParm;
import io.github.kloping.little_web.annotations.WebRestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author github.kloping
 */
@WebRestController
public class RestController0 {
    public RestController0() {
        System.out.println(getClass().getSimpleName() + "=>created");
    }

    private boolean verify(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return false;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("key".equals(cookie.getName())) {
                if (cookie.getValue().equals(CallApiPlugin.conf.getPasswd())) {
                    return true;
                }
            }
        }
        return false;
    }

    @RequestMethod("/")
    public void index(@RequestParm("key") String key, HttpServletResponse response) throws IOException {
        if (key != null && key.equals(CallApiPlugin.conf.getPasswd())) {
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
        return CallApiPlugin.conf.getTemplates();
    }

    @RequestMethod("/delete")
    public Object delete(@RequestParm("touch") String touch, HttpServletRequest request) {
        if (!verify(request)) return null;
        Iterator<CallTemplate> iterator = CallApiPlugin.conf.getTemplates().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().touch.equals(touch)) {
                iterator.remove();
                CallApiPlugin.conf = FileInitializeValue.putValues(CallApiPlugin.CONF_FILE.getAbsolutePath(), CallApiPlugin.conf, true);
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
        boolean modify = false;
        CallTemplate template = null;
        for (CallTemplate confTemplate : CallApiPlugin.conf.getTemplates()) {
            if (confTemplate.getTouch().equals(touch)) {
                template = confTemplate;
                modify = true;
                break;
            }
        }
        if (template == null) {
            template = new CallTemplate();
            template.setTouch(touch);
        }
        template.setOut(out);
        template.setOutArgs(outArgs.split(",|;"));
        template.setUrl(url);
        if (proxy != null && !proxy.isEmpty()) {
            template.setProxyIp(proxy.split(":")[0]);
            template.setProxyPort(Integer.valueOf(proxy.split(":")[1]));
        }
        if (!modify) CallApiPlugin.conf.getTemplates().add(template);
        CallApiPlugin.conf = FileInitializeValue.putValues(CallApiPlugin.CONF_FILE.getAbsolutePath(), CallApiPlugin.conf, true);
        return getAll(request);
    }
}
