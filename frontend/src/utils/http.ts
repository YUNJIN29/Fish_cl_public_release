import axios from 'axios'
import { ElMessage } from 'element-plus'

type ApiEnvelope<T> = {
  success: boolean
  message: string
  data: T
}

export const axiosInstance = axios.create({
  baseURL: '',
  timeout: 60000, // 增加到 60 秒，适配 AI 诊断可能的延迟
})

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

axiosInstance.interceptors.response.use(
  (resp) => {
    const payload = resp.data as ApiEnvelope<any>
    if (payload && typeof payload.success === 'boolean') {
      if (!payload.success) {
        ElMessage.error(payload.message || '请求失败')
        return Promise.reject(new Error(payload.message || '请求失败'))
      }
      return payload.data
    }
    return resp.data
  },
  (err) => {
    const msg = err?.response?.data?.message || err.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  },
)

export const http = {
  get: <T>(url: string, params?: any) => axiosInstance.get<any, T>(url, { params }),
  post: <T>(url: string, data?: any) => axiosInstance.post<any, T>(url, data),
  put: <T>(url: string, data?: any) => axiosInstance.put<any, T>(url, data),
  patch: <T>(url: string, data?: any) => axiosInstance.patch<any, T>(url, data),
  delete: <T>(url: string) => axiosInstance.delete<any, T>(url),
}

