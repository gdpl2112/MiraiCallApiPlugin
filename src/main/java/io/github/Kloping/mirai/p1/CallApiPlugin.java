package io.github.Kloping.mirai.p1;

import io.github.kloping.initialize.FileInitializeValue;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.console.MiraiConsoleImplementation;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author github.kloping
 */
public class CallApiPlugin extends JavaPlugin {

    public static final CallApiPlugin INSTANCE = new CallApiPlugin();

    public static Conf conf = null;

    public CallApiPlugin() {
        super(new JvmPluginDescriptionBuilder("io.github.Kloping.mirai.p1.CallApiPlugin", "1.8").info("调用自定义API插件").build());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        loadConf();
        CommandManager.INSTANCE.registerCommand(CommandLine0.INSTANCE, true);
        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost() {
            @Override
            public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
                super.handleException(context, exception);
            }

            @EventHandler
            public void onMessage(GroupMessageEvent event) {
                onMessage0(event);
            }

            private void onMessage0(MessageEvent event) {
                if (!conf.getPermType().equals(Conf.ALL))
                    return;
                if (event.getMessage().size() > 1) {
                    String text = toText(event.getMessage());
                    Message message = Worker.call(text, event.getSubject().getId(), event.getSender().getId());
                    if (message != null) {
                        event.getSubject().sendMessage(message);
                    }
                }
            }

            @EventHandler
            public void onMessage(FriendMessageEvent event) {
                onMessage0(event);
            }
        });
    }

    private String toText(MessageChain m) {
        StringBuilder sb = new StringBuilder();
        for (SingleMessage singleMessage : m) {
            if (singleMessage instanceof PlainText) {
                sb.append(((PlainText) singleMessage).getContent());
            } else if (singleMessage instanceof At) {
                sb.append("[@").append(((At) singleMessage).getTarget()).append("]");
            }
        }
        return sb.toString();
    }

    @Override
    public void onLoad(PluginComponentStorage storage) {
        super.onLoad(storage);
    }

    public static File CONF_FILE;

    public static void loadConf() {
        conf = new Conf();
        String path = MiraiConsoleImplementation.getInstance().getRootPath().toFile().getAbsolutePath();
        CONF_FILE = new File(path, "conf/callApi/conf.json");
        CONF_FILE.getParentFile().mkdirs();
        conf = FileInitializeValue.getValue(CONF_FILE.getAbsolutePath(), conf, true);
        Worker.init();
    }
}
