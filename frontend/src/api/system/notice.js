import request from '@/utils/request'

// 查询公告列表
export function listNotice(query) {
  return request({
    url: '/system/notice/list',
    method: 'get',
    params: query
  })
}

// 查询用户可见的公告列表
export function listUserNotice(query) {
  return request({
    url: '/system/notice/user/list',
    method: 'get',
    params: query
  })
}

// 查询公告详细
export function getNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'get'
  })
}

// 新增公告
export function addNotice(data) {
  return request({
    url: '/system/notice',
    method: 'post',
    data: data
  })
}

// 修改公告
export function updateNotice(data) {
  return request({
    url: '/system/notice',
    method: 'put',
    data: data
  })
}

// 删除公告
export function delNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'delete'
  })
}

// 标记公告为已读
export function markAsRead(noticeId) {
  return request({
    url: '/system/notice/read/' + noticeId,
    method: 'post'
  })
}

// 获取未读公告数量
export function getUnreadCount() {
  return request({
    url: '/system/notice/unread/count',
    method: 'get'
  })
}