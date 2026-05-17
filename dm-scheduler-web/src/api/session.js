import request from '@/utils/request'

export const getSessionList = () => {
  return request.get('/session/list')
}

/** 分页查询场次 */
export const getSessionPage = (params) => {
  return request.get('/session/page', { params })
}

export const getSessionById = (id) => {
  return request.get(`/session/detail/${id}`)
}

export const addSession = (data) => {
  return request.post('/session/add', data)
}

export const updateSession = (data) => {
  return request.put('/session/update', data)
}

export const deleteSession = (id) => {
  return request.delete(`/session/delete/${id}`)
}

export const startSession = (id) => {
  return request.put(`/session/start/${id}`)
}

export const completeSession = (id) => {
  return request.put(`/session/complete/${id}`)
}

/** 智能调度下周：自动生成场次并同步生成关联DM排班 */
export const autoAssignNextWeek = () => {
  return request.post('/session/auto-assign-next-week')
}
