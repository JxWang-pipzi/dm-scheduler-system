import request from '@/utils/request'

export const getReservationList = () => {
  return request.get('/reservation')
}

export const getReservationsByUserId = (userId) => {
  return request.get(`/reservation/user/${userId}`)
}

export const getReservationsBySessionId = (sessionId) => {
  return request.get(`/reservation/session/${sessionId}`)
}

export const getReservationById = (id) => {
  return request.get(`/reservation/${id}`)
}

export const addReservation = (data) => {
  return request.post('/reservation', data)
}

export const updateReservation = (data) => {
  return request.put('/reservation', data)
}

export const deleteReservation = (id) => {
  return request.delete(`/reservation/${id}`)
}

export const cancelReservation = (id) => {
  return request.put(`/reservation/cancel/${id}`)
}

export const countConfirmedReservations = (sessionId) => {
  return request.get(`/reservation/count/${sessionId}`)
}
