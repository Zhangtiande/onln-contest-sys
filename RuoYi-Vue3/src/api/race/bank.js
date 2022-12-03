import request from '@/utils/request'

// 查询题库列表
export function listBank(query) {
  return request({
    url: '/race/bank/list',
    method: 'get',
    params: query
  })
}

// 查询题库详细
export function getBank(questionId) {
  return request({
    url: '/race/bank/' + questionId,
    method: 'get'
  })
}

// 新增题库
export function addBank(data) {
  return request({
    url: '/race/bank',
    method: 'post',
    data: data
  })
}

// 修改题库
export function updateBank(data) {
  return request({
    url: '/race/bank',
    method: 'put',
    data: data
  })
}

// 删除题库
export function delBank(questionId) {
  return request({
    url: '/race/bank/' + questionId,
    method: 'delete'
  })
}
