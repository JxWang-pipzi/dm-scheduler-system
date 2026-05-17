import request from '@/utils/request'

export default {
  sendNotification(data) {
    return request.post('/notification/send', data)
  },
  getNotificationsByUser(userId) {
    return request.get('/notification/list', { params: { userId } })
  },
  getSystemNotifications() {
    return request.get('/notification/system')
  },
  markAsRead(id) {
    return request.put(`/notification/read/${id}`)
  },
  countUnreadByUser(userId) {
    return request.get('/notification/unread-count', { params: { userId } })
  }
}
