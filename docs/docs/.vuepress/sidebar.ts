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
        text: "采集方式",
        icon: "ability",
        collapsable: false,
        link: "/guide/monitor/way"
      },
      {
        text: "micrometer集成步骤",
        icon: "config",
        collapsable: false,
        link: "/guide/monitor/prometheus_grafana"
      },
    ],
  },

  {
    text: "三方框架集成",
    icon: "plugin",
    prefix: "/guide",
    children: [
      {
        text: "已集成框架",
        icon: "plugin",
        collapsable: false,
        link: "/guide/middleware/middleware"
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
        icon: "note",
        collapsable: false,
        link: "/guide/other/release"
      },
    ],
  },
]);
