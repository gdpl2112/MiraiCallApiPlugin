package io.github.Kloping.mirai.p1;

import java.util.ArrayList;
import java.util.List;

/**
 * @author github.kloping
 */
public class Conf {
    public static final String ALL = "all";
    public static final String CONSOLE = "console";
    private String splitChar = " ";
    private String permType = ALL;
    private List<CallTemplate> templates = new ArrayList<>();
    private String proxyIp;
    private Integer proxyPort;
    private Integer port = 20042;
    private String passwd = "123456";

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getSplitChar() {
        return splitChar;
    }

    public void setSplitChar(String splitChar) {
        this.splitChar = splitChar;
    }

    public String getPermType() {
        return permType;
    }

    public void setPermType(String permType) {
        this.permType = permType;
    }

    public List<CallTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<CallTemplate> templates) {
        this.templates = templates;
    }
}
