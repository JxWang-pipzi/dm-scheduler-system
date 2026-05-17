import request from '@/utils/request'

export const getEvaluationList = () => {
  return request.get('/evaluation')
}

export const getEvaluationsByUserId = (userId) => {
  return request.get(`/evaluation/user/${userId}`)
}

export const getEvaluationsBySessionId = (sessionId) => {
  return request.get(`/evaluation/session/${sessionId}`)
}

export const getEvaluationsByDmId = (dmId) => {
  return request.get(`/evaluation/dm/${dmId}`)
}

export const getEvaluationById = (id) => {
  return request.get(`/evaluation/${id}`)
}

export const addEvaluation = (data) => {
  return request.post('/evaluation', data)
}

export const updateEvaluation = (data) => {
  return request.put('/evaluation', data)
}

export const deleteEvaluation = (id) => {
  return request.delete(`/evaluation/${id}`)
}

export const getAverageRatingByDmId = (dmId) => {
  return request.get(`/evaluation/rating/dm/${dmId}`)
}
