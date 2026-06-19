<template>
  <AppLayout>
    <div class="page-container">
      <div style="display:grid;grid-template-columns:repeat(5,1fr);gap:14px;margin-bottom:20px;">
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">待核验</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#f59e0b;">{{ countBy('PENDING_VERIFY') }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">核验通过</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#10b981;">{{ countBy('VERIFY_PASSED') + countBy('BUYER_CONFIRMED') }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">核验驳回</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#ef4444;">{{ countBy('VERIFY_REJECTED') + countBy('REJECTED') }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">待流转资金岗</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#3b82f6;">{{ countBy('BUYER_CONFIRMED') }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">已放款</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#10b981;">{{ countBy('LOANED') }}</div></el-card>
      </div>

      <div class="page-header">
        <div class="page-title">🛡️ 风控复核工作台</div>
        <div style="display:flex;gap:8px;">
          <el-radio-group v-model="filter" size="default" @change="loadList">
            <el-radio-button value="ALL">全部</el-radio-button>
            <el-radio-button value="PENDING_VERIFY">待核验</el-radio-button>
            <el-radio-button value="VERIFY_PASSED">待回执/流转</el-radio-button>
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
              <div style="font-size:12px;color:#6b7280;">卖方：</div><div style="font-weight:600;">{{ row.sellerName }}</div>
              <div style="font-size:12px;color:#6b7280;margin-top:4px;">债务人：</div><div>{{ row.debtorName }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="160" align="right">
          <template #default="{row}">
            <div>总：<b style="color:#1d4ed8;">¥ {{ formatMoney(row.totalAmount) }}</b></div>
            <div style="color:#6b7280;font-size:12px;">转：¥ {{ formatMoney(row.transferAmount) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="风控/买方状态" width="200" align="center">
          <template #default="{row}">
            <div>
              <el-tag :type="statusMap[row.status]?.color" effect="dark" size="small">{{ statusMap[row.status]?.label }}</el-tag>
            </div>
            <div style="margin-top:6px;font-size:12px;">
              <el-tag v-if="row.buyerReceipt === true" type="success" size="small">✅ 买方已回执</el-tag>
              <el-tag v-else-if="row.buyerReceipt === false" type="danger" size="small">❌ 买方未回执</el-tag>
              <el-tag v-else type="info" size="small">未登记回执</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="客户经理" width="110" />
        <el-table-column prop="submitTime" label="提交时间" width="160">
          <template #default="{row}">{{ formatDate(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{row}">
            <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING_VERIFY'" size="small" link type="success" @click="openVerify(row,true)">核验通过</el-button>
            <el-button v-if="row.status === 'PENDING_VERIFY'" size="small" link type="danger" @click="openVerify(row,false)">核验驳回</el-button>
            <el-button v-if="row.status === 'VERIFY_PASSED' || row.status === 'BUYER_CONFIRMED'" size="small" link type="warning" @click="openReceipt(row)">登记买方回执</el-button>
            <el-button v-if="row.status === 'BUYER_CONFIRMED' && row.buyerReceipt" size="small" link type="primary" @click="transferFund(row)">流转至资金岗</el-button>
          </template>
        </el-table-column>
      </el-table>

      <ReceivableDetail v-model="detailVisible" :data="current" role="RISK" @refresh="loadList" />

      <el-dialog v-model="verifyVisible" :title="verifyPassed ? '风控核验通过' : '风控核验驳回'" width="600px">
        <el-descriptions :column="1" border size="small" style="margin-bottom:16px;">
          <el-descriptions-item label="应收账款编号">{{ current?.receivableNo }}</el-descriptions-item>
          <el-descriptions-item label="卖方/债务人">{{ current?.sellerName }} → {{ current?.debtorName }}</el-descriptions-item>
          <el-descriptions-item label="转让金额">¥ {{ formatMoney(current?.transferAmount) }}</el-descriptions-item>
        </el-descriptions>
        <el-form label-width="100px">
          <el-form-item :label="verifyPassed ? '通过意见' : '驳回原因'" required>
            <el-input v-model="verifyOpinion" type="textarea" :rows="5" :placeholder="verifyPassed ? '请填写风控通过意见、额度使用说明等' : '请填写驳回原因、补充要求等'" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="verifyVisible = false">取消</el-button>
          <el-button :type="verifyPassed ? 'success' : 'danger'" @click="doVerify">确认{{ verifyPassed ? '通过' : '驳回' }}</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="receiptVisible" title="登记买方确认回执" width="600px">
        <el-descriptions :column="1" border size="small" style="margin-bottom:16px;">
          <el-descriptions-item label="债务人（买方）">{{ current?.debtorName }}</el-descriptions-item>
          <el-descriptions-item label="应收账款编号">{{ current?.receivableNo }}</el-descriptions-item>
        </el-descriptions>
        <el-form label-width="120px">
          <el-form-item label="买方回执状态" required>
            <el-radio-group v-model="receiptConfirmed">
              <el-radio :label="true" border>✅ 买方已确认债务并同意转让</el-radio>
              <el-radio :label="false" border>❌ 买方不确认/有异议</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="回执详情" required>
            <el-input v-model="receiptRemark" type="textarea" :rows="5" placeholder="请填写回执详情，例如：收到买方确认函编号、联系人、电话、确认的应付金额等；若买方有异议，写明异议内容。" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="receiptVisible = false">取消</el-button>
          <el-button type="primary" @click="doReceipt">登记回执</el-button>
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
import { listReceivables, verifyReceivable, recordBuyerReceipt, transferToFund } from '../api'
import { statusMap, formatMoney, formatDate } from '../utils/constants'

const loading = ref(false)
const list = ref([])
const filter = ref('ALL')
const detailVisible = ref(false)
const current = ref(null)
const verifyVisible = ref(false)
const receiptVisible = ref(false)
const verifyPassed = ref(true)
const verifyOpinion = ref('')
const receiptConfirmed = ref(true)
const receiptRemark = ref('')

const filteredList = computed(() => filter.value === 'ALL' ? list.value : list.value.filter(r => r.status === filter.value))
const countBy = (st) => list.value.filter(r => r.status === st).length

const loadList = async () => {
  loading.value = true
  try { list.value = await listReceivables() } finally { loading.value = false }
}
onMounted(loadList)

const openDetail = (row) => { current.value = row; detailVisible.value = true }

const openVerify = (row, passed) => {
  current.value = row
  verifyPassed.value = passed
  verifyOpinion.value = passed
    ? `经核查，该笔应收账款交易背景真实，发票已全部验真；债务人${row.debtorName}资质优良，历史回款记录良好；转让金额在审批额度范围内，同意办理保理融资。`
    : ''
  verifyVisible.value = true
}

const doVerify = async () => {
  if (!verifyOpinion.value.trim()) return ElMessage.warning('请填写核验意见')
  await verifyReceivable(current.value.id, { passed: verifyPassed.value, opinion: verifyOpinion.value })
  ElMessage.success(verifyPassed.value ? '风控核验通过，请继续登记买方回执' : '已驳回，客户经理可修改后重新提交')
  verifyVisible.value = false
  loadList()
}

const openReceipt = (row) => {
  current.value = row
  receiptConfirmed.value = row.buyerReceipt === null || row.buyerReceipt === undefined ? true : !!row.buyerReceipt
  receiptRemark.value = row.buyerReceiptRemark || '已收到买方采购部盖章确认函，确认应付账款真实有效并同意该笔债权转让至我司。'
  receiptVisible.value = true
}

const doReceipt = async () => {
  if (!receiptRemark.value.trim()) return ElMessage.warning('请填写回执详情')
  await recordBuyerReceipt(current.value.id, { confirmed: receiptConfirmed.value, remark: receiptRemark.value })
  ElMessage.success('买方回执已登记' + (receiptConfirmed.value ? '，确认后可流转至资金岗' : ''))
  receiptVisible.value = false
  loadList()
}

const transferFund = async (row) => {
  try {
    await ElMessageBox.confirm(`确认将应收账款【${row.receivableNo}】流转至资金岗？\n\n前提：买方已确认回执。`, '流转确认', { type: 'info' })
    await transferToFund(row.id)
    ElMessage.success('已流转至资金岗，等待放款处理')
    loadList()
  } catch (e) { if (e !== 'cancel') {} }
}
</script>
