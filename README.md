## 自定义 调用 API 插件

[下载](https://github.com/gdpl2112/MiraiCallApiPlugin/releases)

启动后生成配置文件

```json
{
  //权限类型  有 console all  当为 console 时只能从命令调用 all 则所有都可以
  "permType": "console",
  //输入 参数分隔符
  "splitChar": " ",
  //api 调用模板
  "templates": [
  ]
}
```

### templates 如何配置

示例涩图配置

```json

{
  "permType": "all",
  "splitChar": " ",
  "templates": [
    {
      "out": "<Pic:$1>",
      "outArgs": [
        "pic"
      ],
      "touch": "涩图",
      "url": "http://iw233.fgimax2.fgnwctvip.com/API/Ghs.php?type=json"
    }
  ]
}
```

- $1 $2 即参数1 参数2 outArgs 指定返回的 数据为 参数N
- $qid 表示发送者id
- $gid 表示所处群id

转换后内部通过转换成message

- 图片格式 \<Pic:路径>
- At格式 \<At:QQ号>
- 语音格式 \<Audio:路径>

见 [详情](https://github.com/gdpl2112/MiraiCallApiPlugin/blob/master/src/main/java/io/github/Kloping/mirai/p1/Parse.java#L11)

配置后 在群聊使用 /callApi call 涩图 或在群聊/好友 发送 涩图 即可触发

### 复杂的返回参数

以下是 https://api.vvhan.com/api/weather?city=徐州&type=week 该 API 返回的数据

```json
{
  "data": {
    "yesterday": {
      "date": "30日星期三",
      "high": "高温 4℃",
      "fx": "西南风",
      "low": "低温 -1℃",
      "fl": "",
      "type": "雨夹雪"
    },
    "city": "西安",
    "forecast": [
      {
        "date": "31日星期四",
        "high": "高温 7℃",
        "fengli": "",
        "low": "低温 -6℃",
        "fengxiang": "西南风",
        "type": "小雪"
      },
      {
        "date": "1日星期五",
        "high": "高温 7℃",
        "fengli": "",
        "low": "低温 -4℃",
        "fengxiang": "东北风",
        "type": "多云"
      },
      {
        "date": "2日星期六",
        "high": "高温 7℃",
        "fengli": "",
        "low": "低温 -3℃",
        "fengxiang": "西南风",
        "type": "多云"
      },
      {
        "date": "3日星期天",
        "high": "高温 10℃",
        "fengli": "",
        "low": "低温 -1℃",
        "fengxiang": "南风",
        "type": "多云"
      },
      {
        "date": "4日星期一",
        "high": "高温 8℃",
        "fengli": "",
        "low": "低温 -3℃",
        "fengxiang": "东北风",
        "type": "多云"
      }
    ],
    "ganmao": "昼夜温差很大，易发生感冒，请注意适当增减衣服，加强自我防护避免感冒。",
    "wendu": "2"
  },
  "status": 1000,
  "desc": "OK"
}
```

```json

{
  "permType": "all",
  "splitChar": " ",
  "templates": [
    {
      "out": "$1:$2\n$3:$4\n$5:$6\n",
      "outArgs": [
        "data.forecast[0].date",
        "data.forecast[0].type",
        "data.forecast[1].date",
        "data.forecast[1].type",
        "data.forecast[2].date",
        "data.forecast[2].type"
      ],
      "touch": "未来天气",
      "url": "https://api.vvhan.com/api/weather?city=$1&type=week"
    }
  ]
}
```

配置后 在群聊/好友 发送 未来天气 <城市名> 即可触发

### 最后送上实用配置

功能含:

- 涩图
- 星座运势 <星座>
- 未来天气 <城市>
- QQ凶吉 <QQ号>

```json
{
  "permType": "all",
  "splitChar": " ",
  "templates": [
    {
      "out": "<Pic:$1>",
      "outArgs": [
        "pic"
      ],
      "touch": "涩图",
      "url": "http://iw233.fgimax2.fgnwctvip.com/API/Ghs.php?type=json"
    },
    {
      "out": "$1",
      "outArgs": [
        "data"
      ],
      "touch": "星座运势",
      "url": "https://api.iyk0.com/xzys/?msg=$1"
    },
    {
      "out": "$1:$2\n$3:$4\n$5:$6\n",
      "outArgs": [
        "data.forecast[0].date",
        "data.forecast[0].type",
        "data.forecast[1].date",
        "data.forecast[1].type",
        "data.forecast[2].date",
        "data.forecast[2].type"
      ],
      "touch": "未来天气",
      "url": "https://api.vvhan.com/api/weather?city=$1&type=week"
    },
    {
      "out": "<Audio:$1>",
      "outArgs": [
        "mp3"
      ],
      "touch": "小妲己",
      "url": "http://api.weijieyue.cn/api/xiaodaji/api.php?msg=$1"
    },
    {
      "out": "QQ:$1\n评语:$2\n凶吉:$3\n性格:$4",
      "outArgs": [
        "qq",
        "pingyu",
        "xiongji",
        "xingge"
      ],
      "touch": "QQ凶吉",
      "url": "http://api.weijieyue.cn/api/qq/xiongji.php?qq=$qid"
    }
  ]
}
```
