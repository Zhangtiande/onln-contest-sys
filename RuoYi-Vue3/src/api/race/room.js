import request from '@/utils/request'

// 查询竞赛房间列表
export function listRoom(query) {
  return request({
    url: '/race/room/list',
    method: 'get',
    params: query
  })
}

// 查询竞赛房间详细
export function getRoom(roomId) {
  return request({
    url: '/race/room/' + roomId,
    method: 'get'
  })
}

// 新增竞赛房间
export function addRoom(data) {
  return request({
    url: '/race/room',
    method: 'post',
    data: data
  })
}

// 修改竞赛房间
export function updateRoom(data) {
  return request({
    url: '/race/room',
    method: 'put',
    data: data
  })
}

// 删除竞赛房间
export function delRoom(roomId) {
  return request({
    url: '/race/room/' + roomId,
    method: 'delete'
  })
}
