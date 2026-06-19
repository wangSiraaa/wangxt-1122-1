<template>
  <AppLayout>
    <div class="page-container">
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-bottom:20px;">
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">待放款</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#f59e0b;">{{ pendingCount }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">待放款金额</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#f59e0b;">¥ {{ formatMoney(pendingAmount) }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">已放款笔数</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#10b981;">{{ loanedCount }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">累计放款金额</div><div style="font-size:22px;font-weight:700;margin-top:6px;color:#10b981;">¥ {{ formatMoney(loanedAmount) }}</div></el-card>
      </div>

      <div class="page-header">
        <div class="page-title">💰 资金岗放款工作台</div>
        <div style="display:flex;gap:8px;">
          <el-radio-group v-model="filter" size="default" @change="loadList">
            <el-radio-button value="ALL">全部</el-radio-button>
            <el-radio-button value="PENDING_LOAN">待放款</el-radio-button>
            <el-radio-button value="LOANED">已放款</el-radio-button>
          </el-radio-group>
          <el-button :icon="RefreshRight" @click="loadList">刷新</el-button>
        </div>
      </div>

      <el-table :data="filteredList" v-loading="loading" border stripe @row-dblclick="openDetail">
        <el-table-column prop="receivableNo" label="编号" width="200" />
        <el-table-column label="卖方 → 买方（债务人）" min-width="300">
          <template #default="{row}">
            <div style="line-height:1.6;">
              <div><span style="font-size:12px;color:#6b7280;">卖方：</span><b>{{ row.sellerName }}</b></div>
              <div style="margin-top:4px;"><span style="font-size:12px;color:#6b7280;">债务人：</span>{{ row.debtorName }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额 / 放款" width="200" align="right">
          <template #default="{row}">
            <div style="line-height:1.7;">
              <div>总额：<b style="color:#1d4ed8;">¥ {{ formatMoney(row.totalAmount) }}</b></div>
              <div style="font-size:12px;">转让：¥ {{ formatMoney(row.transferAmount) }}</div>
              <div v-if="row.loanAmount" style="font-size:12px;color:#059669;">已放：¥ {{ formatMoney(row.loanAmount) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="买方回执" width="130" align="center">
          <template #default="{row}">
            <el-tag v-if="row.buyerReceipt === true" type="success" effect="dark">✅ 已回执</el-tag>
            <el-tag v-else type="danger" effect="dark">❌ 未回执</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{row}"><el-tag :type="statusMap[row.status]?.color" effect="dark">{{ statusMap[row.status]?.label }}</el-tag></template>
        </el-table-column>
        <el-table-column label="经办人" width="180">
          <template #default="{row}">
            <div style="font-size:12px;line-height:1.6;">
              <div>经理：{{ row.managerName }}</div>
              <div>风控：{{ row.riskName }}</div>
              <div>资金：<b>{{ row.fundName || '-' }}</b></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="放款凭证 / 时间" width="200">
          <template #default="{row}">
            <div style="font-size:12px;line-height:1.6;">
              <div>{{ row.loanVoucherNo || '-' }}</div>
              <div style="color:#6b7280;">{{ formatDate(row.loanTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{row}">
            <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING_LOAN'" size="small" link type="success" @click="openLoan(row)">发起放款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <ReceivableDetail v-model="detailVisible" :data="current" role="FUND" @refresh="loadList" />

      <el-dialog v-model="loanVisible" title="发起放款" width="600px" destroy-on-close>
        <el-descriptions :column="1" border size="small" style="margin-bottom:16px;">
          <el-descriptions-item label="应收账款编号">{{ current?.receivableNo }}</el-descriptions-item>
          <el-descriptions-item label="卖方（融资方）">{{ current?.sellerName }}</el-descriptions-item>
          <el-descriptions-item label="转让金额"><b style="color:#1d4ed8;">¥ {{ formatMoney(current?.transferAmount) }}</b></el-descriptions-item>
          <el-descriptions-item label="买方回执状态">
            <el-tag :type="current?.buyerReceipt ? 'success' : 'danger'" effect="dark">{{ current?.buyerReceipt ? '已确认' : '未确认' }}</el-tag>
            <span v-if="!current?.buyerReceipt" style="color:#dc2626;margin-left:8px;font-size:12px;">※ 买方未回执，系统将禁止放款</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-form :model="loanForm" label-width="120px" label-position="right">
          <el-form-item label="放款金额" required>
            <el-input-number v-model="loanForm.loanAmount" :min="0.01" :max="current?.transferAmount || 0" :precision="2" style="width:100%;" />
            <div style="font-size:12px;color:#6b7280;margin-top:4px;">最大可放：¥ {{ formatMoney(current?.transferAmount) }}</div>
          </el-form-item>
          <el-form-item label="放款凭证号" required>
            <el-input v-model="loanForm.loanVoucherNo" placeholder="例如：FK-2025-0618-0001" />
          </el-form-item>
          <el-form-item label="放款说明" required>
            <el-input v-model="loanForm.loanRemark" type="textarea" :rows="4" placeholder="请填写放款账户、利率、期限、核心企业等关键信息" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="loanVisible = false">取消</el-button>
          <el-button type="success" @click="doLoan">确认放款</el-button>
        </template>
      </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import ReceivableDetail from '../components/ReceivableDetail.vue'
import { listReceivables, grantLoan } from '../api'
import { statusMap, formatMoney, formatDate } from '../utils/constants'
import dayjs from 'dayjs'

const loading = ref(false)
const list = ref([])
const filter = ref('ALL')
const detailVisible = ref(false)
const loanVisible = ref(false)
const current = ref(null)
const loanForm = reactive({ loanAmount: 0, loanVoucherNo: '', loanRemark: '' })

const filteredList = computed(() => filter.value === 'ALL' ? list.value : list.value.filter(r => r.status === filter.value))
const pendingCount = computed(() => list.value.filter(r => r.status === 'PENDING_LOAN').length)
const pendingAmount = computed(() => list.value.filter(r => r.status === 'PENDING_LOAN').reduce((s, r) => s + (r.transferAmount || 0), 0))
const loanedCount = computed(() => list.value.filter(r => r.status === 'LOANED').length)
const loanedAmount = computed(() => list.value.filter(r => r.status === 'LOANED').reduce((s, r) => s + (r.loanAmount || 0), 0))

const loadList = async () => {
  loading.value = true
  try { list.value = await listReceivables() } finally { loading.value = false }
}
onMounted(loadList)

const openDetail = (row) => { current.value = row; detailVisible.value = true }

const openLoan = (row) => {
  current.value = row
  loanForm.loanAmount = row.transferAmount
  loanForm.loanVoucherNo = 'FK-' + dayjs().format('YYYYMMDD') + '-' + String(row.id).padStart(4, '0')
  loanForm.loanRemark = `放款至${row.sellerName}指定回款账户；利率年化6.5%，期限6个月；第一还款来源为${row.debtorName}到期付款。`
  loanVisible.value = true
}

const doLoan = async () => {
  if (!loanForm.loanAmount || loanForm.loanAmount <= 0) return ElMessage.warning('请填写放款金额')
  if (!loanForm.loanVoucherNo.trim()) return ElMessage.warning('请填写放款凭证号')
  if (!loanForm.loanRemark.trim()) return ElMessage.warning('请填写放款说明')
  try {
    await ElMessageBox.confirm(`确认放款¥${formatMoney(loanForm.loanAmount)}？\n\n买方回执：${current.value.buyerReceipt ? '已确认' : '未确认'}\n\n系统将校验：买方必须已回执。`, '放款确认', { type: 'warning', confirmButtonText: '确认放款' })
    await grantLoan(current.value.id, loanForm)
    ElMessage.success(`放款成功，凭证号：${loanForm.loanVoucherNo}`)
    loanVisible.value = false
    loadList()
  } catch (e) { if (e !== 'cancel') {} }
}
</script>
