import request from '@/utils/request'

export function listGoods(query) {
  return request({
    url: '/business/goods/list',
    method: 'get',
    params: query
  })
}

export function getGoods(goodsId) {
  return request({
    url: '/business/goods/' + goodsId,
    method: 'get'
  })
}

export function updateGoods(data) {
  return request({
    url: '/business/goods',
    method: 'put',
    data: data
  })
}

export function delGoods(goodsIds) {
  return request({
    url: '/business/goods/' + goodsIds,
    method: 'delete'
  })
}
