import request from '@/utils/request'

/** 分页查询操作日志 */
export const getOperationLogPage = (params) => {
    return request.get('/operation-log/page', { params })
}
