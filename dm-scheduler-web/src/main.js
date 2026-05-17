import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import request from './utils/request'
import permissionDirective from './directives/permission'

// 将axios实例挂载到Vue原型上
Vue.prototype.$axios = request

// 修复 el-dialog 首次点击被吞的全局根因：
// 让 v-modal 遮罩与 dialog wrapper 在同一层叠上下文内
ElementUI.Dialog.props.modalAppendToBody.default = false

Vue.use(ElementUI)
Vue.directive('permission', permissionDirective)
Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
