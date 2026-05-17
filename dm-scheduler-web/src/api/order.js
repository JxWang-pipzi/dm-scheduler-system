import request from '@/utils/request'

export const getOrderById = (id) => {
  return request.get(`/order/${id}`)
}

export const getOrderByOrderNo = (orderNo) => {
  return request.get(`/order/orderNo/${orderNo}`)
}

export const getOrdersByUserId = (userId) => {
  return request.get(`/order/user/${userId}`)
}

export const getOrdersBySessionId = (sessionId) => {
  return request.get(`/order/session/${sessionId}`)
}

export const getOrderPage = (params) => {
  return request.get('/order/page', { params })
}

export const addOrder = (data) => {
  return request.post('/order', data)
}

export const updateOrder = (data) => {
  return request.put('/order', data)
}

export const deleteOrder = (id) => {
  return request.delete(`/order/${id}`)
}

export const payOrder = (id, payMethod) => {
  return request.put(`/order/pay/${id}`, null, { params: { payMethod } })
}
