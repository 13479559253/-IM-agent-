import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['token'] = token
  }
  return config
})

service.interceptors.response.use(
  (res) => {
    const { code, msg, data } = res.data 
    if(code === 1003){
      localStorage.removeItem('token')
      router.replace('/login')
      ElMessage.error('登录已失效，请重新登录')
      return Promise.reject()
    }
    return { code, data, msg }
  },
  (err) => {
    return Promise.reject(err)
  }
)


export { service }
