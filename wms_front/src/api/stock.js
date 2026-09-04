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