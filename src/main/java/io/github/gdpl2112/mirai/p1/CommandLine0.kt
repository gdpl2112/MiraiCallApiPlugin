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

    @Description("调用api")
    @SubCommand("call")
    suspend fun CommandSender.callApi(str: String) {
        val m = this.subject?.let { this.user?.let { it1 -> Worker.call(str, it.id, it1.id) } };
        if (m == null) {
            sendMessage("调用失败")
        } else {
            sendMessage(m)
        }
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
}