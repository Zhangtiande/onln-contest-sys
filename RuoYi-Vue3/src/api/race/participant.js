import request from '@/utils/request'

// 查询比赛参与人员列表
export function listParticipant(query) {
  return request({
    url: '/race/participant/list',
    method: 'get',
    params: query
  })
}

// 查询比赛参与人员详细
export function getParticipant(participantId) {
  return request({
    url: '/race/participant/' + participantId,
    method: 'get'
  })
}

// 新增比赛参与人员
export function addParticipant(data) {
  return request({
    url: '/race/participant',
    method: 'post',
    data: data
  })
}

// 修改比赛参与人员
export function updateParticipant(data) {
  return request({
    url: '/race/participant',
    method: 'put',
    data: data
  })
}

// 删除比赛参与人员
export function delParticipant(participantId) {
  return request({
    url: '/race/participant/' + participantId,
    method: 'delete'
  })
}
