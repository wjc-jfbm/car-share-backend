import request from '@/utils/request'

// 获取首页统计数据
export function getDashboard() {
  return request({
    url: '/business/statistics/dashboard',
    method: 'get'
  })
}

// 获取近7天订单趋势
export function getOrderTrend() {
  return request({
    url: '/business/statistics/orderTrend',
    method: 'get'
  })
}

// 获取拼车状态分布
export function getCarStatusDist() {
  return request({
    url: '/business/statistics/carStatusDist',
    method: 'get'
  })
}

// 获取订单状态分布
export function getOrderStatusDist() {
  return request({
    url: '/business/statistics/orderStatusDist',
    method: 'get'
  })
}

// 获取最近拼车活动
export function getRecentCars() {
  return request({
    url: '/business/statistics/recentCars',
    method: 'get'
  })
}

// 获取最近订单
export function getRecentOrders() {
  return request({
    url: '/business/statistics/recentOrders',
    method: 'get'
  })
}
