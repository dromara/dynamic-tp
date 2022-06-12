import { defineAsyncComponent } from 'vue'

export const layoutComponents = {
  "404": defineAsyncComponent(() => import("/Users/yanhom/opensource/dynamic-tp/docs/node_modules/vuepress-theme-hope/lib/client/layouts/404.js")),
  "Layout": defineAsyncComponent(() => import("/Users/yanhom/opensource/dynamic-tp/docs/node_modules/vuepress-theme-hope/lib/client/layouts/Layout.js")),
  "Slide": defineAsyncComponent(() => import("/Users/yanhom/opensource/dynamic-tp/docs/node_modules/vuepress-theme-hope/lib/client/layouts/Slide.js")),
}
