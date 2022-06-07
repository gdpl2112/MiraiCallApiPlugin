package io.github.Kloping.mirai.p1;

/**
 * @author github.kloping
 */
public class CallTemplate {
    public String touch;
    public String url;
    public String out;
    public String[] outArgs;
    public String err;
    private String proxyIp;
    private Integer proxyPort;

    public String getProxyIp() {
        if (proxyIp == null) return "";
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public Integer getProxyPort() {
        if (proxyPort == null) return 0;
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String[] getOutArgs() {
        return outArgs;
    }

    public void setOutArgs(String[] outArgs) {
        this.outArgs = outArgs;
    }

    public String getTouch() {
        return touch;
    }

    public void setTouch(String touch) {
        this.touch = touch;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }
}
