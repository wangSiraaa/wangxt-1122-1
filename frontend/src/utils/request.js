import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('factoring_user') || 'null')
  if (user) {
    config.headers['X-Username'] = user.username
  }
  return config
})

request.interceptors.response.use(
  (resp) => {
    const res = resp.data
    if (res && res.code === 200) {
      return res.data
    }
    ElMessage.error(res?.msg || '请求失败')
    return Promise.reject(new Error(res?.msg || '请求失败'))
  },
  (err) => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      localStorage.removeItem('factoring_user')
      window.location.href = '/#/login'
    }
    ElMessage.error(err.message || '网络错误')
    return Promise.reject(err)
  }
)

export default request
