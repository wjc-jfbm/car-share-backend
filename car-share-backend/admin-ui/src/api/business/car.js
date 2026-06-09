import request from '@/utils/request'

export function listCar(query) {
  return request({
    url: '/business/car/list',
    method: 'get',
    params: query
  })
}

export function getCar(carId) {
  return request({
    url: '/business/car/' + carId,
    method: 'get'
  })
}

export function updateCar(data) {
  return request({
    url: '/business/car',
    method: 'put',
    data: data
  })
}

export function delCar(carIds) {
  return request({
    url: '/business/car/' + carIds,
    method: 'delete'
  })
}
