export const themeData = {
  "blog": {},
  "encrypt": {},
  "pure": false,
  "darkmode": "switch",
  "themeColor": {
    "blue": "#087CFA",
    "red": "#FE2857",
    "green": "#21D789",
    "orange": "#FC801D",
    "pink": "#FF318C",
    "lightBlue": "#07C3F2"
  },
  "fullscreen": true,
  "locales": {
    "/": {
      "blog": {},
      "repoDisplay": true,
      "navbarIcon": true,
      "navbarAutoHide": "mobile",
      "hideSiteNameonMobile": true,
      "sidebar": [
        {
          "text": "简介",
          "icon": "guide",
          "prefix": "/guide",
          "children": [
            {
              "text": "项目背景",
              "icon": "ask",
              "collapsable": false,
              "link": "/guide/introduction/background"
            },
            {
              "text": "功能特性",
              "icon": "enum",
              "collapsable": false,
              "link": "/guide/introduction/functions"
            },
            {
              "text": "架构设计",
              "icon": "structure",
              "collapsable": false,
              "link": "/guide/introduction/architecture"
            }
          ]
        },
        {
          "text": "快速开始",
          "icon": "hot",
          "prefix": "/guide",
          "children": [
            {
              "text": "使用步骤",
              "icon": "launch",
              "collapsable": false,
              "link": "/guide/use/quick-start"
            },
            {
              "text": "maven依赖",
              "icon": "install",
              "collapsable": false,
              "link": "/guide/use/maven"
            },
            {
              "text": "配置文件",
              "icon": "config",
              "collapsable": false,
              "link": "/guide/use/config"
            },
            {
              "text": "代码使用",
              "icon": "code",
              "collapsable": false,
              "link": "/guide/use/code"
            }
          ]
        },
        {
          "text": "通知报警",
          "icon": "notice",
          "prefix": "/guide",
          "children": [
            {
              "text": "通知",
              "icon": "notice",
              "collapsable": false,
              "link": "/guide/notice/notice"
            },
            {
              "text": "报警",
              "icon": "warn",
              "collapsable": false,
              "link": "/guide/notice/alarm"
            }
          ]
        },
        {
          "text": "监控",
          "icon": "eye",
          "prefix": "/guide",
          "children": [
            {
              "text": "采集方式",
              "icon": "ability",
              "collapsable": false,
              "link": "/guide/monitor/way"
            },
            {
              "text": "micrometer集成步骤",
              "icon": "config",
              "collapsable": false,
              "link": "/guide/monitor/prometheus_grafana"
            }
          ]
        },
        {
          "text": "三方框架集成",
          "icon": "plugin",
          "prefix": "/guide",
          "children": [
            {
              "text": "已集成框架",
              "icon": "plugin",
              "collapsable": false,
              "link": "/guide/middleware/middleware"
            }
          ]
        },
        {
          "text": "其他",
          "icon": "more",
          "prefix": "/guide",
          "children": [
            {
              "text": "加群交流",
              "icon": "wechat",
              "collapsable": false,
              "link": "/guide/other/contact"
            },
            {
              "text": "相关文章",
              "icon": "note",
              "collapsable": false,
              "link": "/guide/other/articles"
            },
            {
              "text": "发版记录",
              "icon": "note",
              "collapsable": false,
              "link": "/guide/other/release"
            }
          ]
        }
      ],
      "sidebarIcon": true,
      "headerDepth": 2,
      "lang": "zh-CN",
      "navbarLocales": {
        "langName": "简体中文",
        "selectLangText": "选择语言",
        "selectLangAriaLabel": "选择语言"
      },
      "metaLocales": {
        "author": "作者",
        "date": "写作日期",
        "origin": "原创",
        "views": "访问量",
        "category": "分类",
        "tag": "标签",
        "readingTime": "阅读时间",
        "words": "字数",
        "toc": "此页内容",
        "prev": "上一页",
        "next": "下一页",
        "lastUpdated": "上次编辑于",
        "contributors": "贡献者",
        "editLink": "编辑此页"
      },
      "outlookLocales": {
        "themeColor": "主题色",
        "darkmode": "外观",
        "fullscreen": "全屏"
      },
      "encryptLocales": {
        "title": "文章已加密",
        "placeholder": "输入密码",
        "remember": "记住密码",
        "errorHint": "请输入正确的密码"
      },
      "routeLocales": {
        "404msg": [
          "这里什么也没有",
          "我们是怎么来到这儿的？",
          "这 是 四 零 四 !",
          "看起来你访问了一个失效的链接"
        ],
        "back": "返回上一页",
        "home": "带我回家",
        "openInNewWindow": "Open in new window"
      },
      "footer": "MIT Licensed  | Theme by <a href=\"https://vuepress-theme-hope.github.io\">vuepress-theme-hope</a> ",
      "copyright": "Copyright © 2022-present <a href=\"https://github.com/lyh200\">yanhom</a>",
      "author": {
        "name": "yanhom",
        "url": "https://gitee.com/dromara/dynamic-tp"
      },
      "logo": "/logo.png",
      "navbar": [
        {
          "text": "主页",
          "icon": "home",
          "link": "/"
        },
        {
          "text": "文档",
          "icon": "read",
          "link": "/guide/introduction/background"
        },
        {
          "text": "接入指南",
          "icon": "launch",
          "link": "/guide/use/quick-start"
        },
        {
          "text": "加入社群",
          "icon": "wechat",
          "link": "/guide/other/contact"
        },
        {
          "text": "Gitee",
          "icon": "gitee",
          "link": "https://gitee.com/dromara/dynamic-tp"
        },
        {
          "text": "Github",
          "icon": "github",
          "link": "https://github.com/dromara/dynamic-tp"
        }
      ],
      "lastUpdated": true,
      "docsDir": "dtp-docs/docs",
      "displayFooter": true,
      "pageInfo": [
        "Author",
        "Original",
        "Date",
        "Category",
        "Tag",
        "ReadingTime"
      ]
    }
  }
}
