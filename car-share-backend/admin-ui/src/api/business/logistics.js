import request from '@/utils/request'

export function listLogistics(query) {
  return request({
    url: '/business/logistics/list',
    method: 'get',
    params: query
  })
}

export function getLogistics(id) {
  return request({
    url: '/business/logistics/' + id,
    method: 'get'
  })
}

export function getLogisticsByCarId(carId) {
  return request({
    url: '/business/logistics/car/' + carId,
    method: 'get'
  })
}

export function shipLogistics(data) {
  return request({
    url: '/business/logistics',
    method: 'post',
    data: data
  })
}

export function updateLogistics(data) {
  return request({
    url: '/business/logistics',
    method: 'put',
    data: data
  })
}

export function updateLogisticsStatus(id, status) {
  return request({
    url: '/business/logistics/' + id + '/status',
    method: 'put',
    data: { status }
  })
}

export function delLogistics(ids) {
  return request({
    url: '/business/logistics/' + ids,
    method: 'delete'
  })
}
