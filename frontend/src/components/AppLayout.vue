<template>
  <div>
    <div style="display:flex;align-items:center;justify-content:space-between;height:60px;padding:0 24px;background:#fff;border-bottom:1px solid #e5e7eb;">
      <div style="display:flex;align-items:center;gap:14px;">
        <div style="font-size:28px;">🏛️</div>
        <div>
          <div style="font-size:17px;font-weight:700;color:#1f2937;">商业保理应收账款转让核验系统</div>
          <div style="font-size:12px;color:#6b7280;">{{ roleLabel }}工作台</div>
        </div>
      </div>
      <div style="display:flex;align-items:center;gap:16px;">
        <el-button size="small" @click="$router.push('/login')">切换账号</el-button>
        <el-avatar :size="34" style="background:#3b82f6;">{{ user?.realName?.charAt(0) || 'U' }}</el-avatar>
        <div style="line-height:1.3;">
          <div style="font-size:13px;font-weight:600;">{{ user?.realName }}</div>
          <div style="font-size:11px;color:#6b7280;">{{ roleLabel }} · {{ user?.username }}</div>
        </div>
        <el-button size="small" type="danger" plain @click="logout">退出</el-button>
      </div>
    </div>
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout as apiLogout } from '../api'
import { roleMap } from '../utils/constants'

const user = computed(() => JSON.parse(localStorage.getItem('factoring_user') || 'null'))
const roleLabel = computed(() => roleMap[user.value?.role] || '')
const router = useRouter()

const logout = () => {
  try { apiLogout() } catch (e) {}
  localStorage.removeItem('factoring_user')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>
