import request from '../utils/request'

// 分页查询商品
export function getProductPage(params) {
  return request.get('/product/page', { params })
}

// 查询所有上架商品（用于出入库选择）
export function getAllProducts() {
  return request.get('/product/list')
}

// 新增商品
export function createProduct(data) {
  return request.post('/product', data)
}

// 修改商品（后端是 POST /{id}，不是 PUT！）
export function updateProduct(id, data) {
  return request.post(`/product/${id}`, data)
}

// 删除商品
export function deleteProduct(id) {
  return request.delete(`/product/${id}`)
}