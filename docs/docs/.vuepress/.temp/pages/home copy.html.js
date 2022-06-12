export const data = {
  "key": "v-62e97819",
  "path": "/home%20copy.html",
  "title": "首页",
  "lang": "zh-CN",
  "frontmatter": {
    "home": true,
    "icon": "home",
    "title": "首页",
    "heroImage": "/logo1.png",
    "heroText": "dynamic-tp",
    "tagline": "基于配置中心的轻量级动态线程池",
    "actions": [
      {
        "text": "快速上手 💡",
        "link": "/guide/use/quick-start",
        "type": "primary"
      },
      {
        "text": "功能 🛠",
        "link": "/guide/feature"
      },
      {
        "text": "历史变更 🔍",
        "link": "/guide/history"
      }
    ],
    "features": [
      {
        "title": "动态调参",
        "icon": "customize",
        "details": "在运行时动态调整线程池参数，包括核心线程数、最大线程数、空闲线程超时时间、任务队列大小等",
        "link": "https://vuepress-theme-hope.github.io/v2/zh/guide/markdown/"
      },
      {
        "title": "通知报警",
        "icon": "notice",
        "details": "目前支持调参通知、活性、队列容量、拒绝策略、超时共六类通知报警维度，在运行时实时+定时检测，触发阈值进行推送",
        "link": "https://vuepress-theme-hope.github.io/v2/zh/guide/layout/slides"
      },
      {
        "title": "运行监控",
        "icon": "eye",
        "details": "定时采集线程池运行指标数据，支持json long输出、micrometer、endpoint三种指标数据输出方式，可灵活选择",
        "link": "https://vuepress-theme-hope.github.io/v2/zh/guide/layout/"
      },
      {
        "title": "三方包集成",
        "icon": "grid",
        "details": "集成三方中间件线程池管理，已接入dubbo、rocketmq、tomcat、undertow、hystrix、jetty等组件线程池管理",
        "link": "https://vuepress-theme-hope.github.io/v2/zh/guide/feature/comment.html"
      }
    ],
    "head": [
      [
        "meta",
        {
          "name": "keywords",
          "content": "Restful Fast Request,idea插件,http client,Restful API"
        }
      ],
      [
        "meta",
        {
          "name": "description",
          "content": "Restful Fast Request 一个基于IDEA的类似postman的restful api工具包插件,可以根据已有的方法帮助您快速生成url和params,一个API调试工具+API管理工具,支持springmvc、springboot、java-rs"
        }
      ],
      [
        "meta",
        {
          "property": "og:url",
          "content": "https://vuepress-theme-hope-v2-demo.mrhope.site/home%20copy.html"
        }
      ],
      [
        "meta",
        {
          "property": "og:title",
          "content": "首页"
        }
      ],
      [
        "meta",
        {
          "property": "og:type",
          "content": "website"
        }
      ],
      [
        "meta",
        {
          "property": "og:locale",
          "content": "zh-CN"
        }
      ]
    ],
    "copyright": "Copyright © 2022-present <a href=\"https://github.com/lyh200\">yanhom</a>",
    "footer": "MIT Licensed  | Theme by <a href=\"https://vuepress-theme-hope.github.io\">vuepress-theme-hope</a>",
    "summary": ""
  },
  "excerpt": "",
  "headers": [],
  "readingTime": {
    "minutes": 0,
    "words": 0
  },
  "filePathRelative": "home copy.md"
}

if (import.meta.webpackHot) {
  import.meta.webpackHot.accept()
  if (__VUE_HMR_RUNTIME__.updatePageData) {
    __VUE_HMR_RUNTIME__.updatePageData(data)
  }
}

if (import.meta.hot) {
  import.meta.hot.accept(({ data }) => {
    __VUE_HMR_RUNTIME__.updatePageData(data)
  })
}
