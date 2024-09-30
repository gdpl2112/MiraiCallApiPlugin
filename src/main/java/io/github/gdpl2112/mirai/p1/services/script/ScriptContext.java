package io.github.gdpl2112.mirai.p1.services.script;

import io.github.gdpl2112.mirai.p1.services.DgSerializer;
import io.github.kloping.url.UrlUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;

import java.io.ByteArrayInputStream;

/**
 * script 交互对象
 * 预设脚本环境变量: context
 *
 * @author github.kloping
 */
public interface ScriptContext {
    /**
     * 获取bot
     *
     * @return
     */
    Bot getBot();

    /**
     * 获取元数据
     *
     * @return
     */
    MessageChain getRaw();

    /**
     * 发送字符串
     *
     * @param str
     */
    void send(String str);

    /**
     * 发送至所在环境
     *
     * @param message
     */
    void send(Message message);

    /**
     * 获得一个 Builder
     *
     * @return
     */
    default MessageChainBuilder builder() {
        return new MessageChainBuilder();
    }

    /**
     * 获得一个 转发行
     *
     * @return
     */
    ForwardMessageBuilder forwardBuilder();

    /**
     * 创建音乐分享消息
     *
     * @param kind
     * @param title
     * @param summer
     * @param jumUrl
     * @param picUrl
     * @param url
     * @return
     */
    default MusicShare createMusicShare(String kind, String title, String summer, String jumUrl, String picUrl, String url) {
        return new MusicShare(MusicKind.valueOf(kind), title, summer, jumUrl, picUrl, url);
    }

    /**
     * 上传图片
     *
     * @param url
     * @return
     */
    default Image uploadImage(String url) {
        try {
            byte[] bytes = UrlUtils.getBytesFromHttpUrl(url);
            Image image = Contact.uploadImage(getBot().getAsFriend(), new ByteArrayInputStream(bytes));
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 构建 文本
     *
     * @param text
     * @return
     */
    default PlainText newPlainText(String text) {
        return new PlainText(text);
    }

    default SuperFace toSuperFace(int id) {
        return SuperFace.from(new Face(id));
    }

    /**
     * 反向 str 解析为 Message
     *
     * @param msg
     * @return
     */
    default Message deSerialize(String msg) {
        return DgSerializer.stringDeserializeToMessageChain(msg, getBot(), getBot().getAsFriend());
    }

    /**
     * 从id获取MessageChain 可用于直接发送 <br>
     * !! 仅能获取2小时以内的数据
     *
     * @param id
     * @return
     */
    MessageChain getMessageChainById(int id);

    /**
     * 发送者ID
     *
     * @return
     */
    User getSender();

    /**
     * 发送环境id 一般为 群id
     *
     * @return
     */
    Contact getSubject();

    /**
     * 所处环境 <a href="https://github.com/gdpl2112/dg-bot/blob/master/js-api.md"> 说明</a>
     *
     * @return
     */
    String getType();
}
