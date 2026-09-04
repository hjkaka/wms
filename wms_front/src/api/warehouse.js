import request from '../utils/request'

// 分页查询仓库
export function getWarehousePage(params) {
  return request.get('/warehouse/page', { params })
}

// 查询所有仓库（用于下拉选择）
export function getAllWarehouses() {
  return request.get('/warehouse/list')
}

// 新增仓库
export function createWarehouse(data) {
  return request.post('/warehouse', data)
}

// 修改仓库（后端用 PUT /{id}）
export function updateWarehouse(id, data) {
  return request.put(`/warehouse/${id}`, data)
}

// 删除仓库
export function deleteWarehouse(id) {
  return request.delete(`/warehouse/${id}`)
}