import request from '../utils/request'

// ===== s3-3 盘点流程 =====

// 盘点单分页列表（按 状态/关键字=单号或仓库）
export function getStockTakePage(params) {
  return request.get('/stocktake/page', { params })
}

// 创建盘点单（自动纳入所选仓库有库存商品作账面快照）
export function createStockTake(data) {
  return request.post('/stocktake', data)
}

// 盘点单详情
export function getStockTake(id) {
  return request.get(`/stocktake/${id}`)
}

// 盘点明细列表（商品/账面/实盘/盈亏）
export function getStockTakeItems(id) {
  return request.get(`/stocktake/${id}/items`)
}

// 录入/修改实盘数量（仅草稿；counted 传 null 表示该项未盘点）
export function updateStockTakeItems(id, items) {
  return request.post(`/stocktake/${id}/update`, items)
}

// 过账：按 实盘≠账面 生成盈亏流水并调整库存（MANAGER/ADMIN）
export function postStockTake(id) {
  return request.post(`/stocktake/${id}/post`)
}