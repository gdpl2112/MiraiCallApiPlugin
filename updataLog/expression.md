## 表达式介绍

- 参数表达式
    - $d 例如 $1 $2 用来接收 参数1 参数2
    - $qname 自动替换为 发送者的资料卡昵称
    - $mname 自动替换为 发送者的群聊昵称 获取失败则不替换
    - $gname 自动替换为 群名
    - $gid 自动替换为 群id
    - $qid 自动替换为 发送者qq
    - $all 将请求的所有数据当做参数返回 仅用于 输出参数(outArgs)
    - $url 将请求的网址当做参数返回 应用于重定向时 仅用于 输出参数(outArgs)
    - $number 自动替换为消息中所有数字拼接的字符串


- 输出格式表达
    - \<Pic:$>     表示为一个图片资源 $ 可为网络地址 本地路径
        - 特殊的 当 输入参数为 用JSON表达的数组时 将转化为 forward (转发) 消息 即将数组中每个元素都加载为图片资源并合并转发
    - \<At:$>      表示为一个At $ 仅为qq号 否则失败
    - \<Face:$>    表示为一个QQ face $仅为face id
    - \<Audio:$>  表示为一个语音 $ 可为网络地址 本地路径
    - \<Music:$1,$2,$3,$4,$5,$6> 特殊表达
        - $1 仅为 MusicKind 枚举中值
            - NeteaseCloudMusic
            - QQMusic
            - MiguMusic
            - KugouMusic
            - KuwoMusic
        - $2 消息卡片标题
        - $3 消息卡片内容
        - $4 点击卡片跳转网页 URL
        - $5 消息卡片图片 URL
        - $6 音乐文件 URL

[高级表达式1](./v2.8.md)

部分应用请参考[配置文件](../conf/callApi/conf.json)