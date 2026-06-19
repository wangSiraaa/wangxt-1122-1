export const statusMap = {
  DRAFT: { label: '草稿', color: 'info' },
  PENDING_VERIFY: { label: '待风控核验', color: 'warning' },
  VERIFY_PASSED: { label: '核验通过', color: 'success' },
  VERIFY_REJECTED: { label: '核验驳回', color: 'danger' },
  BUYER_CONFIRMED: { label: '买方已回执', color: 'primary' },
  PENDING_LOAN: { label: '待放款', color: 'warning' },
  LOANED: { label: '已放款', color: 'success' },
  REJECTED: { label: '已驳回', color: 'danger' }
}

export const invoiceStatusMap = {
  NOT_VERIFIED: { label: '未验真', color: 'info' },
  VERIFYING: { label: '验真中', color: 'warning' },
  VERIFIED: { label: '已验真', color: 'success' },
  VERIFY_FAILED: { label: '验真失败', color: 'danger' }
}

export const roleMap = {
  MANAGER: '客户经理',
  RISK: '风控复核',
  FUND: '资金岗'
}

export const formatMoney = (v) => {
  if (v === null || v === undefined || v === '') return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(Number(v))
}

export const formatDate = (v) => {
  if (!v) return '-'
  return v.replace('T', ' ').slice(0, 19)
}
