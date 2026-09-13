import request from '@/utils/request'

// 查询学员可见的公告列表
export function listStudentNotice(query) {
  return request({
    url: '/system/notice/user/list',
    method: 'get',
    params: query
  })
}

// 查询公告详细信息（学员端专用）
export function getNoticeDetail(noticeId) {
  return request({
    url: '/system/notice/user/' + noticeId,
    method: 'get'
  })
}

// 标记公告为已读
export function markNoticeAsRead(noticeId) {
  return request({
    url: '/system/notice/read/' + noticeId,
    method: 'post'
  })
}

// 获取未读公告数量
export function getUnreadNoticeCount() {
  return request({
    url: '/system/notice/unread/count',
    method: 'get'
  })
} 