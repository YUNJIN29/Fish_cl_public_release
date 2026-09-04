<template>
  <div class="wrap">
    <el-card class="card" shadow="never">
      <template #header>
        <div>
          <div class="title">平台账号</div>
          <div class="sub-title">登录后可保存诊断历史、维护养殖资料和回看报告。</div>
        </div>
      </template>
      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="form.username" autocomplete="username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" :autocomplete="mode === 'login' ? 'current-password' : 'new-password'" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item v-if="mode === 'register'" label="昵称">
          <el-input v-model="form.nickname" maxlength="32" show-word-limit placeholder="例如：赣南鱼友" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" :loading="loading" @click="submit">
            {{ mode === 'login' ? '登录' : '注册并登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const mode = ref<'login' | 'register'>('login')
const form = reactive({
  username: '',
  password: '',
  nickname: '',
})

async function submit() {
  loading.value = true
  try {
    if (mode.value === 'login') {
      await auth.login(form.username, form.password)
    } else {
      await auth.register(form.username, form.password, form.nickname || form.username)
    }
    const redirect = (route.query.redirect as string) || '/'
    await router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--c-bg);
  padding: 20px;
}
.card {
  width: 420px;
  border-radius: 8px;
}
.title {
  font-weight: 800;
  font-size: 20px;
  color: var(--c-text);
}
.sub-title {
  margin-top: 6px;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.6;
}
</style>

