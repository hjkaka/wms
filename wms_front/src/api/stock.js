import request from '../utils/request'

// 创建入库单
export function createStockIn(data) {
  return request.post('/stockin', data)
}

// 创建出库单
export function createStockOut(data) {
  return request.post('/stockout', data)
}

// 库存分页查询
export function getStockPage(params) {
  return request.get('/stock/page', { params })
}

// 按商品汇总
export function getStockSummary() {
  return request.get('/stock/summary')
}

// 出入库日报趋势
export function getStockTrend(params) {
  return request.get('/stock/trend', { params })
}

// ===== s3-6 库存流水分页查询 =====
export function getStockLogPage(params) {
  return request.get('/stock/log/page', { params })
}

// ===== s2-2 审核流：出入库单列表 + 审核操作 =====

// 入库单分页
export function getStockInPage(params) {
  return request.get('/stockin/page', { params })
}
// 提交审核
export function submitStockIn(id) {
  return request.post(`/stockin/${id}/submit`)
}
// 撤回
export function withdrawStockIn(id) {
  return request.post(`/stockin/${id}/withdraw`)
}
// 审核通过
export function approveStockIn(id) {
  return request.post(`/stockin/${id}/approve`)
}
// 驳回（退回草稿）
export function rejectStockIn(id) {
  return request.post(`/stockin/${id}/reject`)
}
// 过账（真正动库存）
export function postStockIn(id) {
  return request.post(`/stockin/${id}/post`)
}

// 出库单分页
export function getStockOutPage(params) {
  return request.get('/stockout/page', { params })
}
// 提交审核
export function submitStockOut(id) {
  return request.post(`/stockout/${id}/submit`)
}
// 撤回
export function withdrawStockOut(id) {
  return request.post(`/stockout/${id}/withdraw`)
}
// 审核通过
export function approveStockOut(id) {
  return request.post(`/stockout/${id}/approve`)
}
// 驳回（退回草稿）
export function rejectStockOut(id) {
  return request.post(`/stockout/${id}/reject`)
}
// 过账（真正动库存）
export function postStockOut(id) {
  return request.post(`/stockout/${id}/post`)
}