import request from '@/utils/request'

export default {
  getSessionStatistics() {
    return request.get('/statistics/session')
  },
  getDmStatistics() {
    return request.get('/statistics/dm')
  },
  getScriptStatistics() {
    return request.get('/statistics/script')
  },
  getIncomeStatistics() {
    return request.get('/statistics/income')
  },
  getChartStatistics() {
    return request.get('/statistics/charts')
  }
}
