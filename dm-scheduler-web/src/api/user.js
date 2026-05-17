import request from '@/utils/request'

export const login = (data) => {
  return request.post('/user/login', data)
}

export const register = (data) => {
  return request.post('/user/register', data)
}

export const getUserList = () => {
  return request.get('/user/list')
}

/** 分页查询用户 */
export const getUserPage = (params) => {
  return request.get('/user/page', { params })
}

export const getUserById = (id) => {
  return request.get(`/user/detail/${id}`)
}

export const addUser = (data) => {
  return request.post('/user/add', data)
}

export const updateUser = (data) => {
  return request.put(`/user/update`, data)
}

export const deleteUser = (id) => {
  return request.delete(`/user/delete/${id}`)
}

export const getCurrentUser = () => {
  return request.get('/user/current')
}

export const updateProfile = (data) => {
  return request.put('/user/profile', data)
}

export const updatePassword = (data) => {
  return request.put('/user/password', data)
}
