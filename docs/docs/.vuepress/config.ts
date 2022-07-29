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
    ['script', {src: 'https://unpkg.com/jquery@3.4.1/dist/jquery.min.js'}],
    ['script', {src: 'https://www.layuicdn.com/layer-v3.1.1/layer.js'}],
    ['script', {}, `
         (function () {
            var bp = document.createElement('script');
            var curProtocol = window.location.protocol.split(':')[0];
            if (curProtocol === 'https') {
                bp.src = 'https://zz.bdstatic.com/linksubmit/push.js';
            } else {
                bp.src = 'http://push.zhanzhang.baidu.com/push.js';
            }
            var s = document.getElementsByTagName("script")[0];
            s.parentNode.insertBefore(bp, s);
        })();

        var bp = document.createElement('script');
        bp.src = 'https://www.googletagmanager.com/gtag/js?id=G-FYSG66S4HQ';
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(bp, s);
        (function() {
            window.dataLayer = window.dataLayer || [];

            function gtag() {
                dataLayer.push(arguments);
            }
            gtag('js', new Date());
            gtag('config', 'G-FYSG66S4HQ');
        })();

        $(function() {
         setTimeout(function () {
              imgHover();
         }, 3000);
       });
       function imgHover(){
         $(".com-box-you a img").hover(function() {
              var msg = $(this).attr("msg");
              if (msg) {
                  window.msgLayer = layer.tips(msg, $(this), {
                      tips: 1,
                      time: 0
                  });
              }
          }, function() {
              var index = window.msgLayer;
              setTimeout(function() {
                  layer.close(index);
              }, 1000);
          });
       }
   `]
  ]
});
