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
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author github.kloping
 */
public class CallApiPlugin extends JavaPlugin {

    public static final CallApiPlugin INSTANCE = new CallApiPlugin();

    public CallApiPlugin() {
        super(new JvmPluginDescriptionBuilder("io.github.Kloping.mirai.p1.CallApiPlugin", "1.1").info("调用自定义API插件").build());
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
                    Message m = event.getMessage().get(1);
                    if (m instanceof PlainText) {
                        PlainText text = (PlainText) m;
                        Message message = Worker.call(text.getContent(), event.getSubject().getId(), event.getSender().getId());
                        if (message != null) {
                            event.getSubject().sendMessage(message);
                        }
                    }
                }
            }

            @EventHandler
            public void onMessage(FriendMessageEvent event) {
                onMessage0(event);
            }
        });
    }

    public static Conf conf = null;

    @Override
    public void onLoad(PluginComponentStorage storage) {
        super.onLoad(storage);
    }

    public static void loadConf() {
        conf = new Conf();
        String path = MiraiConsoleImplementation.getInstance().getRootPath().toFile().getAbsolutePath();
        File file = new File(path, "conf/callApi/conf.json");
        file.getParentFile().mkdirs();
        conf = FileInitializeValue.getValue(file.getAbsolutePath(), conf, true);
    }
}
