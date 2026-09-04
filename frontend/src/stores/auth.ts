import { defineStore } from 'pinia'
import { http } from '@/utils/http'

export type RoleName = 'USER' | 'ADMIN'

export interface UserProfile {
  id: number
  username: string
  nickname: string
  avatarUrl?: string | null
  email?: string | null
  phone?: string | null
  farmName?: string | null
  farmLocation?: string | null
  mainSpecies?: string | null
  bio?: string | null
  createdAt?: string
  roles: RoleName[]
}

interface AuthResponse {
  accessToken: string
  me: UserProfile
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    me: null as UserProfile | null,
    meLoaded: false,
  }),
  actions: {
    async login(username: string, password: string) {
      const res = await http.post<AuthResponse>('/api/auth/login', { username, password })
      this.token = res.accessToken
      localStorage.setItem('token', this.token)
      this.me = res.me
      this.meLoaded = true
    },
    async register(username: string, password: string, nickname: string) {
      const res = await http.post<AuthResponse>('/api/auth/register', { username, password, nickname })
      this.token = res.accessToken
      localStorage.setItem('token', this.token)
      this.me = res.me
      this.meLoaded = true
    },
    async fetchMe() {
      const res = await http.get<UserProfile>('/api/auth/me')
      this.me = res
      this.meLoaded = true
    },
    async updateProfile(payload: Partial<UserProfile>) {
      const res = await http.put<UserProfile>('/api/auth/me', payload)
      this.me = res
      this.meLoaded = true
    },
    logout() {
      this.token = ''
      this.me = null
      this.meLoaded = false
      localStorage.removeItem('token')
    },
  },
})

