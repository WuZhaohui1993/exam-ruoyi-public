import request from '@/utils/request'

// 查询请假申请列表
export function listLeaveApplication(query) {
  return request({
    url: '/leave/application/list',
    method: 'get',
    params: query
  })
}

// 查询请假申请详细
export function getLeaveApplication(id) {
  return request({
    url: '/leave/application/' + id,
    method: 'get'
  })
}

// 新增请假申请
export function addLeaveApplication(data) {
  return request({
    url: '/leave/application',
    method: 'post',
    data: data
  })
}

// 修改请假申请
export function updateLeaveApplication(data) {
  return request({
    url: '/leave/application',
    method: 'put',
    data: data
  })
}

// 删除请假申请
export function delLeaveApplication(id) {
  return request({
    url: '/leave/application/' + id,
    method: 'delete'
  })
}

// 导出请假申请
export function exportLeaveApplication(query) {
  return request({
    url: '/leave/application/export',
    method: 'post',
    params: query
  })
}

// 审批请假申请
export function approveLeaveApplication(id, status, approvalComment) {
  return request({
    url: '/leave/application/approve/' + id,
    method: 'put',
    params: {
      status,
      approvalComment
    }
  })
} 