import { defineUserConfig } from "vuepress";
import theme from "./theme";

export default defineUserConfig({
  lang: "zh-CN",
  title: "dynamic-tp",
  description: "DynamicTp，基于配置中心的轻量级动态线程池，内置监控告警功能，可通过SPI自定义扩展实现",
  base: "/",
  theme,
  head: [
    ['meta', { name: 'keywords', content: 'DynamicTp，dynamic-tp，动态线程池，' +
          'ThreadPoolExecutor，美团线程池，线程池，Dynamic ThreadPool，线程池监控，' +
          'Dubbo线程池，RocketMq线程池，线程池调优，线程池告警，线程池参数设置'}],
    ['script', {src: 'https://www.layuicdn.com/layer-v3.1.1/layer.js', async: 'async'}],
    ['script', {src: '/baidu.js', async: 'async'}],
    ['script', {src: 'https://cdn.wwads.cn/js/makemoney.js', async: 'async'}]
  ]
});
