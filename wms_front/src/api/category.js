import request from '../utils/request'

// 分类树形查询
export function getCategoryTree() {
  return request.get('/category/tree')
}

// 新增分类
export function createCategory(data) {
  return request.post('/category', data)
}

// 修改分类
export function updateCategory(id, data) {
  return request.put(`/category/${id}`, data)
}

// 启停分类（启用/禁用）
export function changeCategoryStatus(id, status) {
  return request.put(`/category/${id}/status`, null, { params: { status } })
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/category/${id}`)
}