import request from '@/utils/request'

/** 分页查询DM排班 */
export const getDmSchedulePage = (params) => {
  return request.get('/dm-schedule/page', { params })
}

/** 查询DM某日排班 */
export const getDmScheduleByDmAndDate = (dmId, date) => {
  return request.get(`/dm-schedule/by-dm/${dmId}`, { params: { date } })
}

/** 查询某日所有排班 */
export const getDmScheduleByDate = (date) => {
  return request.get('/dm-schedule/by-date', { params: { date } })
}

/** 根据需求等级推荐DM */
export const recommendDmByLevel = (needDmLevel) => {
  return request.get(`/dm/schedule/recommend/${needDmLevel}`)
}

/** 新增排班 */
export const addDmSchedule = (data) => {
  return request.post('/dm-schedule/add', data)
}

/** 更新排班 */
export const updateDmSchedule = (data) => {
  return request.put('/dm-schedule/update', data)
}

/** 删除排班 */
export const deleteDmSchedule = (id) => {
  return request.delete(`/dm-schedule/delete/${id}`)
}

/** 自动生成下周排班（不关联场次） */
export const autoGenerateNextWeekDmSchedule = () => {
  return request.post('/dm-schedule/auto-generate-next-week')
}
