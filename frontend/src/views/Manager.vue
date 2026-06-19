<template>
  <AppLayout>
    <div class="page-container">
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-bottom:20px;">
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">我的单据</div><div style="font-size:24px;font-weight:700;margin-top:6px;">{{ list.length }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">草稿待完善</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#6366f1;">{{ countBy('DRAFT') + countBy('VERIFY_REJECTED') + countBy('REJECTED') }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">审批中</div><div style="font-size:24px;font-weight:700;margin-top:6px;color:#f59e0b;">{{ countBy('PENDING_VERIFY') + countBy('VERIFY_PASSED') + countBy('BUYER_CONFIRMED') + countBy('PENDING_LOAN') }}</div></el-card>
        <el-card shadow="hover"><div style="font-size:12px;color:#6b7280;">已放款金额</div><div style="font-size:22px;font-weight:700;margin-top:6px;color:#10b981;">¥ {{ formatMoney(loanedAmount) }}</div></el-card>
      </div>

      <div class="page-header">
        <div class="page-title">📒 我的应收账款登记</div>
        <div style="display:flex;gap:8px;">
          <el-button :icon="RefreshRight" @click="loadList">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="openCreate">新建应收账款登记</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe @row-dblclick="openDetail">
        <el-table-column prop="receivableNo" label="编号" width="200" />
        <el-table-column label="卖方" min-width="180">
          <template #default="{row}"><b>{{ row.sellerName }}</b></template>
        </el-table-column>
        <el-table-column label="买方（债务人）" min-width="180">
          <template #default="{row}">{{ row.debtorName }}</template>
        </el-table-column>
        <el-table-column label="总金额" width="140" align="right">
          <template #default="{row}"><b style="color:#1d4ed8;">¥ {{ formatMoney(row.totalAmount) }}</b></template>
        </el-table-column>
        <el-table-column label="转让/放款" width="180" align="center">
          <template #default="{row}">
            <div style="font-size:12px;line-height:1.6;">
              <div>转让：<span style="color:#059669;">¥ {{ formatMoney(row.transferAmount) }}</span></div>
              <div>放款：<span :style="{color: row.loanAmount ? '#059669' : '#9ca3af'}">{{ row.loanAmount ? '¥ '+formatMoney(row.loanAmount) : '-' }}</span></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130" align="center">
          <template #default="{row}"><el-tag :type="statusMap[row.status]?.color" effect="dark">{{ statusMap[row.status]?.label }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{row}">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{row}">
            <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="['DRAFT','VERIFY_REJECTED','REJECTED'].includes(row.status)" size="small" link type="warning" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="['DRAFT','VERIFY_REJECTED','REJECTED'].includes(row.status)" size="small" link type="success" @click="submitRow(row)">提交风控</el-button>
          </template>
        </el-table-column>
      </el-table>

      <ReceivableDetail v-model="detailVisible" :data="current" role="MANAGER" @refresh="loadList" @invoice-verified="loadList" />

      <el-dialog v-model="formVisible" :title="isEdit ? '编辑应收账款登记' : '新建应收账款登记'" width="800px" top="5vh" destroy-on-close>
        <el-form :model="form" label-width="110px" size="default">
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="合同编号"><el-input v-model="form.contractNo" placeholder="如：HT-2025-0001" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="到期日"><el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" style="width:100%;" /></el-form-item></el-col>
          </el-row>
          <el-divider content-position="left">交易双方</el-divider>
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="卖方名称" required><el-input v-model="form.sellerName" placeholder="（债权人）转让方公司全称" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="卖方税号"><el-input v-model="form.sellerTaxNo" /></el-form-item></el-col>
            <el-col :span="12">
              <el-form-item label="债务人名称" required>
                <el-input v-model="form.debtorName" placeholder="（买方）付款方公司全称" />
              </el-form-item>
            </el-col>
            <el-col :span="12"><el-form-item label="债务人税号"><el-input v-model="form.debtorTaxNo" /></el-form-item></el-col>
          </el-row>
          <el-divider content-position="left">金额信息</el-divider>
          <el-row :gutter="16">
            <el-col :span="8"><el-form-item label="应收账款总额" required><el-input-number v-model="form.totalAmount" :min="0" :precision="2" style="width:100%;" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="转让金额" required><el-input-number v-model="form.transferAmount" :min="0" :precision="2" style="width:100%;" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="转让比例">
              <el-input disabled :value="form.totalAmount && form.transferAmount ? ((form.transferAmount / form.totalAmount) * 100).toFixed(2) + '%' : '-'" />
            </el-form-item></el-col>
          </el-row>
          <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>

          <el-divider content-position="left">
            <span>📄 发票登记（{{ form.invoices.length }} 张）</span>
            <el-button size="small" type="primary" link style="margin-left:10px;" @click="addInvoice">+ 新增发票</el-button>
          </el-divider>
          <el-table :data="form.invoices" border size="small">
            <el-table-column label="发票代码" width="130">
              <template #default="{row,$index}"><el-input v-model="row.invoiceCode" size="small" /></template>
            </el-table-column>
            <el-table-column label="发票号码" width="120">
              <template #default="{row}"><el-input v-model="row.invoiceNo" size="small" /></template>
            </el-table-column>
            <el-table-column label="开票日期" width="130">
              <template #default="{row}"><el-date-picker v-model="row.invoiceDate" type="date" value-format="YYYY-MM-DD" size="small" style="width:100%;" /></template>
            </el-table-column>
            <el-table-column label="金额（不含税）" width="150">
              <template #default="{row}"><el-input-number v-model="row.amount" size="small" :min="0" :precision="2" controls-position="right" style="width:100%;" @change="calcInvTotal(row)" /></template>
            </el-table-column>
            <el-table-column label="税额" width="120">
              <template #default="{row}"><el-input-number v-model="row.taxAmount" size="small" :min="0" :precision="2" controls-position="right" style="width:100%;" @change="calcInvTotal(row)" /></template>
            </el-table-column>
            <el-table-column label="价税合计" width="150">
              <template #default="{row}"><el-input v-model="row.totalAmount" size="small" disabled style="background:#f9fafb;" /></template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{$index}"><el-button link type="danger" size="small" @click="form.invoices.splice($index,1)">删除</el-button></template>
            </el-table-column>
          </el-table>
          <div style="margin-top:10px;text-align:right;color:#6b7280;font-size:13px;">
            发票合计价税：<b style="color:#1d4ed8;">¥ {{ formatMoney(invoiceTotal) }}</b>
          </div>
        </el-form>
        <template #footer>
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" @click="saveForm">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import AppLayout from '../components/AppLayout.vue'
import ReceivableDetail from '../components/ReceivableDetail.vue'
import { listReceivables, createReceivable, updateReceivable, submitReceivable } from '../api'
import { statusMap, formatMoney, formatDate } from '../utils/constants'

const loading = ref(false)
const list = ref([])
const detailVisible = ref(false)
const current = ref(null)
const formVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const emptyForm = () => ({
  contractNo: '',
  sellerName: '',
  sellerTaxNo: '',
  debtorName: '',
  debtorTaxNo: '',
  totalAmount: 0,
  transferAmount: 0,
  dueDate: '',
  remark: '',
  invoices: []
})
const form = reactive(emptyForm())

const loanedAmount = computed(() => list.value.filter(r => r.status === 'LOANED').reduce((s, r) => s + (r.loanAmount || 0), 0))
const invoiceTotal = computed(() => form.invoices.reduce((s, i) => s + (i.totalAmount || 0), 0))

const countBy = (st) => list.value.filter(r => r.status === st).length

const loadList = async () => {
  loading.value = true
  try { list.value = await listReceivables() } finally { loading.value = false }
}
onMounted(loadList)

const openDetail = (row) => { current.value = row; detailVisible.value = true }

const openCreate = () => {
  Object.assign(form, emptyForm())
  form.invoices = []
  isEdit.value = false
  editId.value = null
  formVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  editId.value = row.id
  isEdit.value = true
  formVisible.value = true
}

const addInvoice = () => {
  form.invoices.push({ invoiceCode: '', invoiceNo: '', invoiceDate: '', amount: 0, taxAmount: 0, totalAmount: 0 })
}
const calcInvTotal = (row) => { row.totalAmount = (row.amount || 0) + (row.taxAmount || 0) }

const saveForm = async () => {
  if (!form.sellerName || !form.debtorName) return ElMessage.warning('请填写卖方和债务人名称')
  if (!form.totalAmount || form.totalAmount <= 0) return ElMessage.warning('请填写应收账款总额')
  if (!form.invoices.length) return ElMessage.warning('请至少登记一张发票')
  for (const inv of form.invoices) {
    if (!inv.invoiceCode || !inv.invoiceNo) return ElMessage.warning('请完善发票代码和号码')
  }
  const invSum = form.invoices.reduce((s, i) => s + (i.totalAmount || 0), 0)
  if (invSum < Number(form.totalAmount)) return ElMessage.warning(`发票价税合计（${formatMoney(invSum)}）不能小于应收账款总额（${formatMoney(form.totalAmount)}）`)

  const payload = {
    receivable: {
      contractNo: form.contractNo,
      sellerName: form.sellerName,
      sellerTaxNo: form.sellerTaxNo,
      debtorName: form.debtorName,
      debtorTaxNo: form.debtorTaxNo,
      totalAmount: form.totalAmount,
      transferAmount: form.transferAmount,
      dueDate: form.dueDate,
      remark: form.remark
    },
    invoices: form.invoices
  }
  if (isEdit.value) {
    await updateReceivable(editId.value, payload)
    ElMessage.success('修改成功')
  } else {
    await createReceivable(payload)
    ElMessage.success('登记成功，可在发票详情中进行验真')
  }
  formVisible.value = false
  loadList()
}

const submitRow = async (row) => {
  try {
    await ElMessageBox.confirm(`确认提交应收账款【${row.receivableNo}】至风控复核？\n\n系统将校验：所有发票必须已验真。`, '提交确认', { type: 'warning' })
    await submitReceivable(row.id)
    ElMessage.success('已成功提交风控复核')
    loadList()
  } catch (e) { if (e !== 'cancel') {} }
}
</script>
