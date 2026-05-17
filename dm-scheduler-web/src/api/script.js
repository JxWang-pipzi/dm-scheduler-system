import request from '@/utils/request'

export const getScriptList = () => {
  return request.get('/script/list')
}

/** 分页查询剧本 */
export const getScriptPage = (params) => {
  return request.get('/script/page', { params })
}

export const getScriptById = (id) => {
  return request.get(`/script/detail/${id}`)
}

export const addScript = (data) => {
  return request.post('/script/add', data)
}

export const updateScript = (data) => {
  return request.put('/script/update', data)
}

export const deleteScript = (id) => {
  return request.delete(`/script/delete/${id}`)
}

export const getHotScripts = (limit = 4) => {
  return request.get('/script/hot', { params: { limit } })
}
