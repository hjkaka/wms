import request from '../utils/request'

// ===== s3-5 报表 =====

// 库存周转率
export function getTurnover(params) {
  return request.get('/report/turnover', { params })
}

// 呆滞分析
export function getDormant(params) {
  return request.get('/report/dormant', { params })
}

// ABC 分类
export function getAbc(params) {
  return request.get('/report/abc', { params })
}