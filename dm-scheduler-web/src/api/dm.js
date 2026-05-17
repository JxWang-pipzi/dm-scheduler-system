import request from '@/utils/request'

export default {
  getDmList() {
    return request.get('/dm/list')
  },
  /** 分页查询DM */
  getDmPage(params) {
    return request.get('/dm/page', { params })
  },
  getDmDetail(id) {
    return request.get(`/dm/detail/${id}`)
  },
  addDm(dm) {
    return request.post('/dm/add', dm)
  },
  updateDm(dm) {
    return request.put('/dm/update', dm)
  },
  deleteDm(id) {
    return request.delete(`/dm/delete/${id}`)
  },
  getAvailableDms(params) {
    return request.post('/dm/available', params)
  },
  assignDmToSession(params) {
    return request.post('/dm/assign', params)
  },
  getDmSchedule(dmId, startDate, endDate) {
    return request.get(`/dm/schedule/${dmId}`, {
      params: { startDate, endDate }
    })
  },
  autoAssignDm(sessionId) {
    return request.post('/dm/auto-assign', { sessionId })
  }
}
