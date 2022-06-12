import { Vuepress } from '@vuepress/client'

const routeItems = [
  ["v-8daa1a0e","/",{"title":"首页","icon":"home"},["/index.html","/README.md"]],
  ["v-3406509d","/guide/introduction/architecture.html",{"title":"架构设计","icon":"structure"},["/guide/introduction/architecture","/guide/introduction/architecture.md"]],
  ["v-774e7762","/guide/introduction/background.html",{"title":"项目背景","icon":"ask"},["/guide/introduction/background","/guide/introduction/background.md"]],
  ["v-2be5ef72","/guide/introduction/functions.html",{"title":"功能特性","icon":"enum"},["/guide/introduction/functions","/guide/introduction/functions.md"]],
  ["v-f89b2414","/guide/middleware/middleware.html",{"title":"已集成框架","icon":"plugin"},["/guide/middleware/middleware","/guide/middleware/middleware.md"]],
  ["v-0a66322e","/guide/monitor/prometheus_grafana.html",{"title":"micrometer接入流程","icon":"config"},["/guide/monitor/prometheus_grafana","/guide/monitor/prometheus_grafana.md"]],
  ["v-5306456a","/guide/monitor/way.html",{"title":"采集类型","icon":"ability"},["/guide/monitor/way","/guide/monitor/way.md"]],
  ["v-3ee2c8de","/guide/other/articles.html",{"title":"介绍文章","icon":"article"},["/guide/other/articles","/guide/other/articles.md"]],
  ["v-ad6e9978","/guide/other/contact.html",{"title":"加群交流","icon":"wechat"},["/guide/other/contact","/guide/other/contact.md"]],
  ["v-300ff086","/guide/other/release.html",{"title":"发版记录","icon":"note"},["/guide/other/release","/guide/other/release.md"]],
  ["v-d2565d5a","/guide/notice/alarm.html",{"title":"报警","icon":"warn"},["/guide/notice/alarm","/guide/notice/alarm.md"]],
  ["v-540df636","/guide/notice/notice.html",{"title":"通知","icon":"notice"},["/guide/notice/notice","/guide/notice/notice.md"]],
  ["v-cf6119d0","/guide/use/code.html",{"title":"代码使用","icon":"code"},["/guide/use/code","/guide/use/code.md"]],
  ["v-204ad7e3","/guide/use/config.html",{"title":"配置文件","icon":"config"},["/guide/use/config","/guide/use/config.md"]],
  ["v-ced828bc","/guide/use/maven.html",{"title":"maven依赖","icon":"install"},["/guide/use/maven","/guide/use/maven.md"]],
  ["v-bd86dfea","/guide/use/quick-start.html",{"title":"接入步骤","icon":"launch"},["/guide/use/quick-start","/guide/use/quick-start.md"]],
  ["v-3706649a","/404.html",{"title":""},["/404"]],
]

export const pagesRoutes = routeItems.reduce(
  (result, [name, path, meta, redirects]) => {
    result.push(
      {
        name,
        path,
        component: Vuepress,
        meta,
      },
      ...redirects.map((item) => ({
        path: item,
        redirect: path,
      }))
    )
    return result
  },
  [
    {
      name: '404',
      path: '/:catchAll(.*)',
      component: Vuepress,
    }
  ]
)
