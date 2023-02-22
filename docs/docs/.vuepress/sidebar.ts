import {sidebar} from "vuepress-theme-hope";

export default sidebar([
  {
    text: "简介",
    icon: "guide",
    prefix: "/guide",
    children: [
      {
        text: "项目背景",
        icon: "ask",
        collapsable: false,
        link: "/guide/introduction/background"
      },
      {
        text: "功能特性",
        icon: "enum",
        collapsable: false,
        link: "/guide/introduction/functions"
      },
      {
        text: "架构设计",
        icon: "structure",
        collapsable: false,
        link: "/guide/introduction/architecture"
      },
    ],
  },

  {
    text: "快速开始",
    icon: "hot",
    prefix: "/guide",
    children: [
      {
        text: "使用步骤",
        icon: "launch",
        collapsable: false,
        link: "/guide/use/quick-start"
      },
      {
        text: "maven依赖",
        icon: "install",
        collapsable: false,
        link: "/guide/use/maven"
      },
      {
        text: "配置文件",
        icon: "config",
        collapsable: false,
        link: "/guide/use/config"
      },
      {
        text: "代码使用",
        icon: "code",
        collapsable: false,
        link: "/guide/use/code"
      },
      {
        text: "任务包装器",
        icon: "wrap",
        collapsable: false,
        link: "/guide/use/wrapper"
      },
      {
        text: "工具",
        icon: "tool",
        collapsable: false,
        link: "/guide/use/tool"
      }
    ],
  },

  {
    text: "通知报警",
    icon: "notice",
    prefix: "/guide",
    children: [
      {
        text: "调参通知",
        icon: "notice",
        collapsable: false,
        link: "/guide/notice/notice"
      },
      {
        text: "运行报警",
        icon: "warn",
        collapsable: false,
        link: "/guide/notice/alarm"
      },
      {
        text: "推送限流",
        icon: "decorate",
        collapsable: false,
        link: "/guide/notice/ratelimiter"
      },
      {
        text: "邮件通知",
        icon: "at",
        collapsable: false,
        link: "/guide/notice/email"
      }
    ],
  },

  {
    text: "监控",
    icon: "eye",
    prefix: "/guide",
    children: [
      {
        text: "数据采集",
        icon: "ability",
        collapsable: false,
        link: "/guide/monitor/way"
      },
      {
        text: "prometheus+grafana 监控",
        icon: "config",
        collapsable: false,
        link: "/guide/monitor/prometheus_grafana"
      },
      {
        text: "hertzbeat 监控",
        icon: "computer",
        collapsable: false,
        link: "/guide/monitor/hertzbeat"
      }
    ],
  },

  {
    text: "三方框架集成",
    icon: "plugin",
    prefix: "/guide",
    children: [
      {
        text: "tomcat 线程池管理",
        icon: "Apache",
        collapsable: false,
        link: "/guide/middleware/tomcat"
      },
      {
        text: "undertow 线程池管理",
        icon: "safari",
        collapsable: false,
        link: "/guide/middleware/undertow"
      },
      {
        text: "jetty 线程池管理",
        icon: "alias",
        collapsable: false,
        link: "/guide/middleware/jetty"
      },
      {
        text: "dubbo 线程池管理",
        icon: "selection",
        collapsable: false,
        link: "/guide/middleware/dubbo"
      },
      {
        text: "rocketmq 线程池管理",
        icon: "angular",
        collapsable: false,
        link: "/guide/middleware/rocketmq"
      },
      {
        text: "hystrix 线程池管理",
        icon: "bit",
        collapsable: false,
        link: "/guide/middleware/hystrix"
      },
      {
        text: "okhttp3 线程池管理",
        icon: "branch",
        collapsable: false,
        link: "/guide/middleware/okhttp3"
      },
      {
        text: "grpc 线程池管理",
        icon: "chrome",
        collapsable: false,
        link: "/guide/middleware/grpc"
      },
      {
        text: "brpc 线程池管理",
        icon: "snow",
        collapsable: false,
        link: "/guide/middleware/brpc"
      },
      {
        text: "motan 线程池管理",
        icon: "dart",
        collapsable: false,
        link: "/guide/middleware/motan"
      },
      {
        text: "tars 线程池管理",
        icon: "function",
        collapsable: false,
        link: "/guide/middleware/tars"
      },
      {
        text: "sofarpc 线程池管理",
        icon: "linter",
        collapsable: false,
        link: "/guide/middleware/sofa"
      }
    ],
  },

  {
    text: "其他",
    icon: "more",
    prefix: "/guide",
    children: [
      {
        text: "加群交流",
        icon: "wechat",
        collapsable: false,
        link: "/guide/other/contact"
      },
      {
        text: "相关文章",
        icon: "note",
        collapsable: false,
        link: "/guide/other/articles"
      },
      {
        text: "发版记录",
        icon: "list",
        collapsable: false,
        link: "/guide/other/release"
      },
    ],
  },
]);
