<template>
  <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#1e3a8a 0%,#3b82f6 50%,#60a5fa 100%);">
    <div style="width:420px;background:#fff;border-radius:16px;padding:40px 36px;box-shadow:0 20px 60px rgba(0,0,0,0.15);">
      <div style="text-align:center;margin-bottom:28px;">
        <div style="font-size:40px;margin-bottom:12px;">🏛️</div>
        <h1 style="font-size:22px;font-weight:700;margin:0;color:#1f2937;">商业保理应收账款转让核验</h1>
        <p style="margin:8px 0 0;font-size:13px;color:#6b7280;">多岗协同 全流程管控 风险透明</p>
      </div>
      <el-form :model="form" size="large" @keyup.enter="doLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width:100%;height:44px;font-size:15px;" @click="doLogin">登 录</el-button>
      </el-form>
      <el-divider content-position="center" style="margin:24px 0 16px;">演示账号</el-divider>
      <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:10px;">
        <el-button v-for="u in demoUsers" :key="u.role" size="small" @click="quickLogin(u)">
          <div style="line-height:1.4;">
            <div style="font-weight:600;font-size:12px;">{{ u.label }}</div>
            <div style="font-size:11px;color:#6b7280;">{{ u.username }}</div>
          </div>
        </el-button>
      </div>
      <div style="margin-top:18px;padding:12px;background:#eff6ff;border-radius:8px;font-size:12px;color:#1d4ed8;">
        💡 一键演示：<el-link type="primary" @click="executeDemo" style="font-size:12px;">从登记到放款完整流程</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login, runDemo as runDemoApi } from '../api'

const router = useRouter()
const loading = ref(false)
const demoLoading = ref(false)

const form = reactive({ username: 'manager', password: '123456' })

const demoUsers = [
  { role: 'MANAGER', label: '客户经理', username: 'manager' },
  { role: 'RISK', label: '风控复核', username: 'risk' },
  { role: 'FUND', label: '资金岗', username: 'fund' }
]

const rolePath = { MANAGER: '/manager', RISK: '/risk', FUND: '/fund' }

const quickLogin = (u) => {
  form.username = u.username
  form.password = '123456'
  doLogin()
}

const doLogin = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    const user = await login({ username: form.username, password: form.password })
    localStorage.setItem('factoring_user', JSON.stringify(user))
    ElMessage.success(`欢迎回来，${user.realName}（${user.role === 'MANAGER' ? '客户经理' : user.role === 'RISK' ? '风控复核' : '资金岗'}）`)
    router.push(rolePath[user.role])
  } finally {
    loading.value = false
  }
}

const executeDemo = async () => {
  try {
    await ElMessageBox.confirm(
      '将自动执行一笔从登记→验真→提交→风控核验→买方回执→放款的完整演示流程，确认继续？',
      '一键演示',
      { confirmButtonText: '开始演示', cancelButtonText: '取消', type: 'info' }
    )
    demoLoading.value = true
    const result = await runDemoApi()
    ElMessage.success('演示流程执行成功！可使用任意账号登录查看（推荐 manager / 123456）')
    console.log('演示结果', result)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '演示失败')
  } finally {
    demoLoading.value = false
  }
}
</script>
