## 自定义 调用 API 插件

[下载](https://github.com/gdpl2112/MiraiCallApiPlugin/releases)

#### [表达式](./updataLog/expression.md)

### **使用前请检查依赖(lib-tts)是否一同添加至plugins 前往 [releases](https://github.com/gdpl2112/lib-tts/) 查找**
#### **使用前请检查JDK版本,目前仅支持11,其他版本可能出现报错或其他位置情况**

启动后生成配置文件

```json
{
  //权限类型  有 console all  当为 console 时只能从命令调用 all 则所有都可以
  "permType": "console",
  //输入 参数分隔符
  "splitChar": " ",
  //网页管理的端口
  "port": 20042,
  //全局代理ip
  "proxyIp": null,
  //全局代理port
  "proxyPort": 0,
  //网页管理的密码
  "passwd": "123456",
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
      "err": "调用失败",
      "out": "<Pic:$1>",
      "outArgs": [
        "pic[0]"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "随机图片",
      "url": "http://api.iw233.cn/api.php?sort=cat&type=json"
    }
  ]
}
```

- $1 $2 即参数1 参数2 outArgs 指定返回的 数据为 参数N
- $qid 表示发送者id
- $gid 表示所处群id

转换后内部通过转换成message

见 [详情表达式](https://github.com/gdpl2112/MiraiCallApiPlugin#%E8%A1%A8%E8%BE%BE%E5%BC%8F)

配置后 在群聊/好友 发送 随机图片 即可触发

### 复杂的返回参数

<details> 
<summary>
以下是 https://api.vvhan.com/api/weather?city=徐州&type=week 该 API 返回的数据
</summary>

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

</details> 

- 配置后 在群聊/好友 发送 未来天气 <城市名> 即可触发

<details> 
<summary>配置文件</summary> 

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
      "url": "https://api.vvhan.com/api/weather?city=$1&type=week",
      "err": "天气查询失败"
    }
  ]
}
```

</details> 

### 最后送上实用配置

<details> 
<summary>配置文件</summary> 

```json
{
  "passwd": "123456",
  "permType": "all",
  "port": 20042,
  "proxyIp": null,
  "proxyPort": 0,
  "splitChar": " ",
  "templates": [
    {
      "err": "天气查询失败",
      "out": "<At:$qid>\n$1:$2\n$3:$4\n$5:$6\n",
      "outArgs": [
        "data.forecast[0].date",
        "data.forecast[0].type",
        "data.forecast[1].date",
        "data.forecast[1].type",
        "data.forecast[2].date",
        "data.forecast[2].type"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "未来天气",
      "url": "https://api.vvhan.com/api/weather?city=$1&type=week"
    },
    {
      "err": "调用失败",
      "out": "<Pic:$1>",
      "outArgs": [
        "pic[0]"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "随机图片",
      "url": "http://api.iw233.cn/api.php?sort=cat&type=json"
    },
    {
      "err": "调用失败",
      "out": "<Pic:$1>",
      "outArgs": [
        "$url"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "需要ta吗",
      "url": "https://ovooa.com/API/face_need/?QQ=$number"
    },
    {
      "err": "调用失败",
      "out": "<Pic:$1>",
      "outArgs": [
        "[]"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "快手图集",
      "url": "http://kloping.top/api/search/parseImgs?url=$1&type=ks"
    },
    {
      "err": "调用失败",
      "out": "<Pic:$1>",
      "outArgs": [
        "data.[]"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "堆糖搜图",
      "url": "http://kloping.top/api/search/pic?keyword=$1&num=3&type=duit"
    },
    {
      "err": "调用失败",
      "out": "<Music:KugouMusic,$1,$2,https://www.kugou.com/,$3,$4>",
      "outArgs": [
        "data[0].media_name",
        "data[0].author_name",
        "data[0].imgUrl",
        "data[0].songUrl"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "酷狗点歌",
      "url": "http://kloping.top/api/search/song?keyword=$1&type=kugou&n=2"
    },
    {
      "err": null,
      "out": "<At:$qid>\n$1",
      "outArgs": [
        "$all"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "/ping",
      "url": "https://xian.txma.cn/API/sping.php?url=$1"
    },
    {
      "err": null,
      "out": "id：$1\n来自群$2\n的$3\n时间：$call(http://kloping.top/stamp2time?stamp=$4&time=)\n昵称：$5\n信息：$6\n剩余捡起次数：$7",
      "outArgs": [
        "id",
        "gid",
        "sid",
        "time",
        "name",
        "message",
        "state"
      ],
      "proxyIp": "",
      "proxyPort": 0,
      "sw": true,
      "touch": "/捡瓶子",
      "url": "http://kloping.top/api/pickUpBottle"
    }
  ]
}
```

</details> 

### 更多 帮助请查看 [releases](https://github.com/gdpl2112/MiraiCallApiPlugin/releases)