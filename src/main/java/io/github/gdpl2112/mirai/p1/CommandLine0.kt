package io.github.gdpl2112.mirai.p1

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.java.JCompositeCommand

/**
 * @author github.kloping
 */
class CommandLine0 private constructor() : JCompositeCommand(CallApiPlugin.INSTANCE, "callApi") {
    companion object {
        @JvmField
        val INSTANCE = CommandLine0()
    }

    @OptIn(ConsoleFrontEndImplementation::class)
    @Description("重载配置")
    @SubCommand("reload")
    suspend fun CommandSender.callApiReload() {
        CallApiPlugin.loadConf();
        sendMessage("重载完成")
    }

    @Description("开关某一API")
    @SubCommand("switch")
    suspend fun CommandSender.switchApi(@Name("触发词") str: String) {
        for (template in CallApiPlugin.conf.templates) {
            if (template.touch.equals(str.trim())) {
                template.reverseSw()
                sendMessage("\"${template.touch}\"目前处于\"${template.sw}\"状态")
                return
            }
        }
        sendMessage("未发现该API")
    }

    @Description("某一API权限控制")
    @SubCommand("set")
    suspend fun CommandSender.callApiSetPer(
        @Name("触发词") str: String,
        @Name("id") id: String,
        @Name("布尔值") k: Boolean
    ) {
        ManagerConf.INSTANCE.getStateByTouchAndIdDefault(str, id, k)
        ManagerConf.INSTANCE.getTouch2Id2K()[str]?.set(id, k);
        ManagerConf.INSTANCE.apply()
        sendMessage("当前触发词: '${str}'在id: '${id}'状态为${k}")
    }

    @Description("某一API权限查询")
    @SubCommand("get")
    suspend fun CommandSender.callApiGetPer(
        @Name("触发词") str: String,
        @Name("id") id: String,
    ) {
        val k = ManagerConf.INSTANCE.getStateByTouchAndIdDefault(str, id, true)
        sendMessage("当前触发词: '${str}'在id: '${id}'状态为${k}")
    }

    @Description("某一API存在的所有id同时操作权限控制")
    @SubCommand("setAll")
    suspend fun CommandSender.callApiSetAllPer(
        @Name("触发词") str: String,
        @Name("布尔值") k: Boolean
    ) {
        ManagerConf.INSTANCE.getStateByTouchAndIdDefault(str, "f0", k)
        val map = ManagerConf.INSTANCE.getTouch2Id2K()[str]!!
        for (s in map.keys) {
            map[s] = k
        }
        ManagerConf.INSTANCE.apply()
        sendMessage("当前触发词: '${str}'在id状态为${k}")
    }

}