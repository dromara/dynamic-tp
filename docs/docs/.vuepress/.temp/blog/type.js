export const typeMap = {"article":{"/":{"path":"/article/","keys":["v-f89b2414","v-0a66322e","v-5306456a","v-d2565d5a","v-540df636","v-cf6119d0","v-204ad7e3","v-ced828bc","v-bd86dfea","v-3406509d","v-774e7762","v-2be5ef72","v-3ee2c8de","v-ad6e9978","v-300ff086"]}},"encrypted":{"/":{"path":"/encrypted/","keys":[]}},"slide":{"/":{"path":"/slide/","keys":[]}},"star":{"/":{"path":"/star/","keys":["v-f89b2414","v-0a66322e","v-5306456a","v-d2565d5a","v-540df636","v-cf6119d0","v-204ad7e3","v-ced828bc","v-bd86dfea","v-3406509d","v-774e7762","v-2be5ef72","v-3ee2c8de","v-ad6e9978","v-300ff086"]}},"timeline":{"/":{"path":"/timeline/","keys":["v-f89b2414","v-0a66322e","v-5306456a","v-3406509d","v-774e7762","v-2be5ef72","v-d2565d5a","v-540df636","v-3ee2c8de","v-ad6e9978","v-300ff086","v-cf6119d0","v-204ad7e3","v-ced828bc","v-bd86dfea"]}}}

if (import.meta.webpackHot) {
  import.meta.webpackHot.accept()
  if (__VUE_HMR_RUNTIME__.updateBlogType) {
    __VUE_HMR_RUNTIME__.updateBlogType(typeMap)
  }
}

if (import.meta.hot) {
  import.meta.hot.accept(({ typeMap }) => {
    __VUE_HMR_RUNTIME__.updateBlogType(typeMap)
  })
}
