import request from '../utils/request'

// 分页查询用户
export function getUserPage(params) {
  return request.get('/user/page', { params })
}

// 新增用户
export function createUser(data) {
  return request.post('/user', data)
}

// 修改用户（后端 PUT /{id}，密码留空则不改）
export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

// 启停用户（启用/停用）
export function changeUserStatus(id, status) {
  return request.put(`/user/${id}/status`, null, { params: { status } })
}