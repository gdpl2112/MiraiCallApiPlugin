package io.github.gdpl2112.mirai.p1;

import io.github.kloping.file.FileUtils;
import io.github.kloping.serialize.HMLObject;
import net.mamoe.mirai.console.MiraiConsoleImplementation;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author github.kloping
 */
public class ManagerConf {
    public static ManagerConf INSTANCE = new ManagerConf();
    private static File confFile;
    public HashMap<String, HashMap<String, Boolean>> touch2Id2K = new LinkedHashMap<>();

    public HashMap<String, HashMap<String, Boolean>> getTouch2Id2K() {
        return touch2Id2K;
    }

    public void setTouch2Id2K(HashMap<String, HashMap<String, Boolean>> touch2Id2K) {
        this.touch2Id2K = touch2Id2K;
    }

    public Boolean getStateByTouchAndId(String touch, String id) {
        if (touch2Id2K.containsKey(touch)) {
            HashMap<String, Boolean> map = touch2Id2K.get(touch);
            if (map.containsKey(id)) {
                return map.get(id);
            }
        }
        return null;
    }

    public Boolean getStateByTouchAndIdDefault(String touch, String id, Boolean k) {
        if (touch2Id2K.containsKey(touch)) {
            HashMap<String, Boolean> map = touch2Id2K.get(touch);
            if (map.containsKey(id)) {
                return map.get(id);
            } else {
                map.put(id, k);
                touch2Id2K.put(touch,map);
                ManagerConf.INSTANCE.apply();
                return getStateByTouchAndId(touch, id);
            }
        } else {
            HashMap<String, Boolean> map = new HashMap<>();
            map.put(id, k);
            touch2Id2K.put(touch, map);
            ManagerConf.INSTANCE.apply();
            return getStateByTouchAndId(touch, id);
        }
    }

    public ManagerConf() {
        String path = MiraiConsoleImplementation.getInstance().getRootPath().toFile().getAbsolutePath();
        confFile = new File(path, "conf/callApi/conf.hml");
        confFile.getParentFile().mkdirs();
    }

    public void apply() {
        FileUtils.putStringInFile(HMLObject.toHMLString(this), confFile);
    }

    public static void reload() {
        String hml = FileUtils.getStringFromFile(confFile.getAbsolutePath());
        if (hml == null || hml.isEmpty()) return;
        try {
            ManagerConf.INSTANCE = (ManagerConf) HMLObject.parseObject(hml).toJavaObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
