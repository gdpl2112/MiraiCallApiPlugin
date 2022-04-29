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
