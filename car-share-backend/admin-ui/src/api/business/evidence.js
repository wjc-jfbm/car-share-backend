import request from '@/utils/request'

export function listEvidence(query) {
  return request({
    url: '/business/evidence/list',
    method: 'get',
    params: query
  })
}

export function getEvidence(evidenceId) {
  return request({
    url: '/business/evidence/' + evidenceId,
    method: 'get'
  })
}
