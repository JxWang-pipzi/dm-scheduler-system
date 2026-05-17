import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 使用代理后的基础路径
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 如果没有 code 字段，可能是直接返回了数据或者 Blob
    if (res.code === undefined && res instanceof Blob) {
      return res
    }
    
    // 如果返回的状态码不是 200，说明接口报错
    if (res.code !== 200 && res.code !== 0) { // 兼容 code 0 和 200
      Message({
        message: res.message || res.msg || 'Error',
        type: 'error',
        duration: 5 * 1000
      })

      // 鉴权失败：未授权/登录失效
      if (res.code === 401 || res.code === 4010 || res.code === 4011) {
        // 清除 token
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 跳转登录页
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    const config = error && error.config
    const isTimeout = error && (error.code === 'ECONNABORTED' || (error.message || '').includes('timeout'))
    const canRetry = config && config.method === 'get' && isTimeout
    if (canRetry) {
      config.__retryCount = config.__retryCount || 0
      if (config.__retryCount < 2) {
        config.__retryCount += 1
        const delay = 300 * Math.pow(2, config.__retryCount - 1)
        return new Promise(resolve => setTimeout(resolve, delay)).then(() => service(config))
      }
    }
    console.log('err' + error) // for debug
    let message = error.message
    if (error.message === 'Network Error') {
      message = '网络错误，请稍后重试'
    } else if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      message = '请求超时，请稍后重试'
    }
    Message({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
