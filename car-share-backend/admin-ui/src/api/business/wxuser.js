import request from '@/utils/request'

// 查询小程序用户列表
export function listWxUser(query) {
  return request({
    url: '/business/wxuser/list',
    method: 'get',
    params: query
  })
}

// 查询小程序用户详细
export function getWxUser(id) {
  return request({
    url: '/business/wxuser/' + id,
    method: 'get'
  })
}

// 修改小程序用户
export function updateWxUser(data) {
  return request({
    url: '/business/wxuser',
    method: 'put',
    data: data
  })
}

// 删除小程序用户
export function delWxUser(ids) {
  return request({
    url: '/business/wxuser/' + ids,
    method: 'delete'
  })
}
