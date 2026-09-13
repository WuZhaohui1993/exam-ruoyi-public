import request from '@/utils/request'

// 查询考试发布列表
export function listExamination(query) {
  return request({
    url: '/exam/examination/list',
    method: 'get',
    params: query
  })
}

// 查询考试发布详细
export function getExamination(id) {
  return request({
    url: '/exam/examination/' + id,
    method: 'get'
  })
}

// 查询考试授权部门
export function getExamDeptIds(id) {
  return request({
    url: '/exam/examination/depts/' + id,
    method: 'get'
  })
}

// 查询考试发布详细信息（包含统计数据）
export function getExaminationDetails(id) {
  return request({
    url: '/exam/examination/details/' + id,
    method: 'get'
  })
}

// 新增考试发布
export function addExamination(data) {
  return request({
    url: '/exam/examination',
    method: 'post',
    data: data
  })
}

// 修改考试发布
export function updateExamination(data) {
  return request({
    url: '/exam/examination',
    method: 'put',
    data: data
  })
}

// 删除考试发布
export function delExamination(id) {
  return request({
    url: '/exam/examination/' + id,
    method: 'delete'
  })
}

// 发布考试
export function publishExamination(id) {
  return request({
    url: '/exam/examination/publish/' + id,
    method: 'put'
  })
}

// 取消考试
export function cancelExamination(id) {
  return request({
    url: '/exam/examination/cancel/' + id,
    method: 'put'
  })
}

// 恢复为草稿
export function restoreDraftExamination(id) {
  return request({
    url: '/exam/examination/restoreDraft/' + id,
    method: 'put'
  })
}

// 开始考试
export function startExamination(id) {
  return request({
    url: '/exam/examination/start/' + id,
    method: 'put'
  })
}

// 结束考试
export function endExamination(id) {
  return request({
    url: '/exam/examination/end/' + id,
    method: 'put'
  })
}

// 发布考试成绩
export function publishResults(id) {
  return request({
    url: '/exam/examination/publishResults/' + id,
    method: 'put'
  })
}

// 关联证书模板
export function associateCertificate(examId, certificateTemplateId) {
  return request({
    url: '/exam/examination/associateCertificate/' + examId + '/' + certificateTemplateId,
    method: 'put'
  })
}

// 获取考试统计信息
export function getStatistics(id) {
  return request({
    url: '/exam/examination/statistics/' + id,
    method: 'get'
  })
}

// 查询用户可参与的考试列表
export function getAvailableExaminations() {
  return request({
    url: '/exam/examination/available',
    method: 'get'
  })
}

// 导出考试发布
export function exportExamination(query) {
  return request({
    url: '/exam/examination/export',
    method: 'post',
    data: query
  })
}

// ==================== 考试人员管理 ====================

// 分页查询考试人员
export function listExamUsers(examId, query) {
  return request({
    url: '/exam/examination/users/' + examId,
    method: 'get',
    params: query
  })
}

// 获取用户考试成绩详情
export function getExamResult(examId, userId) {
  return request({
    url: '/exam/examination/examResult/' + examId + '/' + userId,
    method: 'get'
  })
}

// 添加考试人员
export function addExamUsers(examId, userIds) {
  return request({
    url: '/exam/examination/addUsers/' + examId,
    method: 'post',
    data: userIds
  })
}

// 移除考试人员
export function removeExamUsers(examId, userIds) {
  return request({
    url: '/exam/examination/removeUsers/' + examId,
    method: 'delete',
    data: userIds
  })
}

// 同步更新考试人员
export function updateExamUsers(examId, userIds) {
  return request({
    url: '/exam/examination/updateUsers/' + examId,
    method: 'put',
    data: userIds
  })
}

// 按部门添加考试人员
export function addExamUsersByDept(examId, deptIds) {
  return request({
    url: '/exam/examination/addUsersByDept/' + examId,
    method: 'post',
    data: deptIds
  })
}

// 按部门添加考试人员（包含子部门）
export function addExamUsersByDeptWithChildren(examId, deptIds) {
  return request({
    url: '/exam/examination/addUsersByDeptWithChildren/' + examId,
    method: 'post',
    data: deptIds
  })
}

// 审核用户报名
export function approveUser(examUserId, status) {
  return request({
    url: '/exam/examination/approveUser/' + examUserId + '/' + status,
    method: 'put'
  })
}

// 批量审核用户报名
export function batchApproveUsers(examUserIds, status) {
  return request({
    url: '/exam/examination/batchApproveUsers/' + status,
    method: 'put',
    data: examUserIds
  })
}

// 导出考试人员
export function exportExamUsers(examId) {
  return request({
    url: '/exam/examination/exportUsers/' + examId,
    method: 'post'
  })
}

// 导出考试成绩统计
export function exportScoreStatistics(examId, params) {
  return request({
    url: '/exam/examination/exportScoreStatistics/' + examId,
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}

// 导出考试成绩明细
export function exportScoreDetails(examId, params) {
  return request({
    url: '/exam/examination/exportScoreDetails/' + examId,
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}

// 重置单次考试记录（保留同一考生的其他历史轮次）
export function resetUserExam(examUserId) {
  return request({
    url: '/exam/examination/reset/' + examUserId,
    method: 'delete'
  })
}

// 人工阅卷
export function manualGrade(examId, userId) {
  return request({
    url: '/exam/examination/manualGrade/' + examId + '/' + userId,
    method: 'put'
  })
}

// 获取考试二维码
export function getExamQrCode(examId, baseUrl) {
  return request({
    url: '/exam/examination/qrcode/' + examId,
    method: 'get',
    params: { baseUrl: baseUrl }
  })
}

// 获取业务概览统计
export function getBusinessStatistics() {
  return request({
    url: '/exam/examination/statistics/business',
    method: 'get'
  })
}
