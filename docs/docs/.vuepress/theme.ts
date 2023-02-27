import { hopeTheme } from "vuepress-theme-hope";
import navbar from "./navbar";
import sidebar from "./sidebar";

export default hopeTheme({
  footer: "MIT Licensed  | Theme by <a href=\"https://vuepress-theme-hope.github.io\">vuepress-theme-hope</a> ",
  copyright: "Copyright © 2022-present <a href=\"https://github.com/yanhom1314\">yanhom</a>",
  hostname: "https://gitee.com/dromara/dynamic-tp",

  author: {
    name: "yanhom",
    url: "https://gitee.com/dromara/dynamic-tp",
  },

  iconAssets: "//at.alicdn.com/t/font_2410206_a0xb9hku9iu.css",

  logo: "/logo.png",

  // navbar
  navbar: navbar,

  // sidebar
  sidebar: sidebar,
  lastUpdated: true,
  backToTop: true,

  docsDir: 'dtp-docs/docs',
  displayFooter: true,

  pageInfo: ["Author", "Original", "Date", "Category", "Tag", "ReadingTime"],

  plugins: {
    mdEnhance: {
      enableAll: true,
      presentation: {
        plugins: ["highlight", "math", "search", "notes", "zoom"],
      },
    },
  },
  fullscreen: true,
  themeColor: {
    blue: "#087CFA",
    red: "#FE2857",
    green: "#21D789",
    orange: "#FC801D",
    pink :"#FF318C",
    lightBlue:"#07C3F2"
  }
});
