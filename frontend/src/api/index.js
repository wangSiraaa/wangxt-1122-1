import request from '../utils/request'

export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')

export const listReceivables = () => request.get('/receivables')
export const getReceivable = (id) => request.get(`/receivables/${id}`)
export const listInvoices = (id) => request.get(`/receivables/${id}/invoices`)
export const listLogs = (id) => request.get(`/receivables/${id}/logs`)

export const createReceivable = (data) => request.post('/receivables', data)
export const updateReceivable = (id, data) => request.put(`/receivables/${id}`, data)
export const verifyInvoice = (id, data) => request.post(`/receivables/${id}/invoice-verify`, data)
export const submitReceivable = (id) => request.post(`/receivables/${id}/submit`)

export const verifyReceivable = (id, data) => request.post(`/receivables/${id}/verify`, data)
export const recordBuyerReceipt = (id, data) => request.post(`/receivables/${id}/buyer-receipt`, data)
export const transferToFund = (id) => request.post(`/receivables/${id}/transfer-fund`)

export const grantLoan = (id, data) => request.post(`/receivables/${id}/loan`, data)

export const runDemo = () => request.post('/demo/run')
