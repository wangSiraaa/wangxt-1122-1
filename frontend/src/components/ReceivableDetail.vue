<template>
  <el-dialog v-model="visible" :title="title" width="900px" top="5vh" destroy-on-close>
    <div v-if="data">
      <el-descriptions :column="2" border size="default" style="margin-bottom:16px;">
        <el-descriptions-item label="应收账款编号" :span="1">{{ data.receivableNo }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="1">
          <el-tag :type="statusMap[data.status]?.color" effect="dark">{{ statusMap[data.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="合同编号">{{ data.contractNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户经理">{{ data.managerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="卖方（债权人）" :span="2">{{ data.sellerName }} <span v-if="data.sellerTaxNo" style="color:#6b7280;">（{{ data.sellerTaxNo }}）</span></el-descriptions-item>
        <el-descriptions-item label="买方（债务人）" :span="2" :class="{'is-locked': data.status === 'LOANED'}">
          {{ data.debtorName }} <span v-if="data.debtorTaxNo" style="color:#6b7280;">（{{ data.debtorTaxNo }}）</span>
          <el-tag v-if="data.status === 'LOANED'" type="danger" size="small" style="margin-left:8px;">🔒 已放款，不可修改债务人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="应收账款总额"><span style="color:#1d4ed8;font-weight:600;">¥ {{ formatMoney(data.totalAmount) }}</span></el-descriptions-item>
        <el-descriptions-item label="转让金额"><span style="color:#059669;font-weight:600;">¥ {{ formatMoney(data.transferAmount) }}</span></el-descriptions-item>
        <el-descriptions-item label="到期日">{{ data.dueDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="放款金额"><span v-if="data.loanAmount" style="color:#059669;font-weight:600;">¥ {{ formatMoney(data.loanAmount) }}</span><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ data.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="📄 发票信息" name="inv">
          <el-table :data="invoices" size="small" border>
            <el-table-column prop="invoiceCode" label="发票代码" width="140" />
            <el-table-column prop="invoiceNo" label="发票号码" width="120" />
            <el-table-column prop="invoiceDate" label="开票日期" width="110" />
            <el-table-column label="金额" width="130">
              <template #default="{row}">¥ {{ formatMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="税额" width="110">
              <template #default="{row}">¥ {{ formatMoney(row.taxAmount) }}</template>
            </el-table-column>
            <el-table-column label="价税合计" width="140">
              <template #default="{row}"><b>¥ {{ formatMoney(row.totalAmount) }}</b></template>
            </el-table-column>
            <el-table-column label="验真状态" width="110">
              <template #default="{row}">
                <el-tag :type="invoiceStatusMap[row.verifyStatus]?.color">{{ invoiceStatusMap[row.verifyStatus]?.label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="验真时间" width="160">
              <template #default="{row}">{{ formatDate(row.verifyTime) }}</template>
            </el-table-column>
            <el-table-column label="验真说明" min-width="180" show-overflow-tooltip>
              <template #default="{row}">{{ row.verifyRemark || '-' }}</template>
            </el-table-column>
          </el-table>
          <div v-if="role === 'MANAGER' && editable" style="margin-top:12px;">
            <el-button type="primary" size="small" :icon="Check" @click="verifyAllInvoices">全部验真</el-button>
            <span style="color:#6b7280;font-size:12px;margin-left:8px;">（系统模拟税务平台验真）</span>
          </div>
        </el-tab-pane>

        <el-tab-pane label="🔍 风控核验 & 买方回执" name="risk">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="风控复核人">
              {{ data.riskName || '-' }}
              <span v-if="data.verifyTime" style="color:#6b7280;margin-left:8px;">{{ formatDate(data.verifyTime) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="核验意见">
              <span v-if="data.riskName">
                <el-tag v-if="data.status === 'VERIFY_PASSED' || data.status === 'BUYER_CONFIRMED' || data.status === 'PENDING_LOAN' || data.status === 'LOANED'" type="success">通过</el-tag>
                <el-tag v-else-if="data.status === 'VERIFY_REJECTED'" type="danger">驳回</el-tag>
                <div style="margin-top:6px;padding:10px;background:#f9fafb;border-radius:6px;white-space:pre-wrap;">{{ data.verifyOpinion || '-' }}</div>
              </span>
              <span v-else style="color:#9ca3af;">暂无核验意见</span>
            </el-descriptions-item>
            <el-descriptions-item label="买方确认回执">
              <span v-if="data.buyerReceipt !== null && data.buyerReceipt !== undefined">
                <el-tag v-if="data.buyerReceipt" type="success">✅ 买方已确认</el-tag>
                <el-tag v-else type="danger">❌ 买方未确认</el-tag>
                <span v-if="data.buyerReceiptOperator" style="color:#6b7280;margin-left:8px;">
                  登记人：{{ data.buyerReceiptOperator }} · {{ formatDate(data.buyerReceiptTime) }}
                </span>
                <div v-if="data.buyerReceiptRemark" style="margin-top:6px;padding:10px;background:#eff6ff;border-radius:6px;white-space:pre-wrap;">{{ data.buyerReceiptRemark }}</div>
              </span>
              <span v-else style="color:#9ca3af;">未登记买方回执</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="💰 放款信息" name="loan">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="资金岗经办人">{{ data.fundName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="放款凭证号">{{ data.loanVoucherNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="放款时间">{{ formatDate(data.loanTime) }}</el-descriptions-item>
            <el-descriptions-item label="放款金额">
              <span v-if="data.loanAmount" style="font-size:18px;color:#059669;font-weight:700;">¥ {{ formatMoney(data.loanAmount) }}</span>
              <span v-else style="color:#9ca3af;">-</span>
            </el-descriptions-item>
            <el-descriptions-item label="放款说明">
              <div v-if="data.loanRemark" style="padding:10px;background:#f0fdf4;border-radius:6px;white-space:pre-wrap;">{{ data.loanRemark }}</div>
              <span v-else style="color:#9ca3af;">-</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="📋 操作日志" name="logs">
          <el-timeline>
            <el-timeline-item v-for="l in logs" :key="l.id" :timestamp="formatDate(l.operateTime)" placement="top" :type="timelineType(l.operation)">
              <el-card shadow="never" style="margin-bottom:4px;">
                <div style="display:flex;align-items:center;gap:8px;">
                  <b>{{ l.operatorName }}</b>
                  <el-tag size="small" type="info">{{ l.operatorRole }}</el-tag>
                  <span style="color:#1d4ed8;">{{ l.operation }}</span>
                </div>
                <div v-if="l.detail" style="margin-top:4px;color:#374151;white-space:pre-wrap;">{{ l.detail }}</div>
              </el-card>
            </el-timeline-item>
            <div v-if="!logs.length" style="text-align:center;padding:30px;color:#9ca3af;">暂无操作日志</div>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </div>
    <template #footer>
      <slot name="footer" />
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { listInvoices, listLogs, verifyInvoice } from '../api'
import { statusMap, invoiceStatusMap, formatMoney, formatDate } from '../utils/constants'

const props = defineProps({
  modelValue: Boolean,
  data: Object,
  role: String
})
const emit = defineEmits(['update:modelValue', 'refresh', 'invoiceVerified'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })
const title = computed(() => props.data?.receivableNo ? `应收账款详情 · ${props.data.receivableNo}` : '应收账款详情')
const activeTab = ref('inv')
const invoices = ref([])
const logs = ref([])

const editable = computed(() => props.data && ['DRAFT', 'VERIFY_REJECTED', 'REJECTED'].includes(props.data.status))

const timelineType = (op) => {
  if (op?.includes('放款')) return 'success'
  if (op?.includes('通过') || op?.includes('确认') || op?.includes('验真通过')) return 'success'
  if (op?.includes('驳回')) return 'danger'
  if (op?.includes('提交') || op?.includes('流转') || op?.includes('登记')) return 'warning'
  return 'primary'
}

watch(() => props.data?.id, async (id) => {
  if (id) {
    invoices.value = await listInvoices(id)
    logs.value = await listLogs(id)
  }
}, { immediate: true })

const verifyAllInvoices = async () => {
  const todo = invoices.value.filter(i => i.verifyStatus !== 'VERIFIED')
  if (!todo.length) return ElMessage.info('所有发票已验真')
  for (const inv of todo) {
    await verifyInvoice(props.data.id, { invoiceId: inv.id })
  }
  ElMessage.success(`成功验真 ${todo.length} 张发票`)
  invoices.value = await listInvoices(props.data.id)
  emit('invoiceVerified')
  emit('refresh')
}
</script>

<style>
.is-locked { background: #fef2f2 !important; }
</style>
