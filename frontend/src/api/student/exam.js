import request from '@/utils/request'

// 查询学员可参加的考试列表
export function getAvailableExaminations(query) {
  return request({
    url: '/student/exam/available',
    method: 'get',
    params: query
  })
}

// 查询学员的所有考试列表
export function getAllExaminations(query) {
  return request({
    url: '/student/exam/list',
    method: 'get',
    params: query
  })
}

// 查询学员考试记录
export function getExamRecords(query) {
  return request({
    url: '/student/exam/records',
    method: 'get',
    params: query
  })
}

// 获取考试详情
export function getExamDetail(examId) {
  return request({
    url: '/student/exam/' + examId,
    method: 'get'
  })
}

// 报名参加考试
export function registerExam(examId) {
  return request({
    url: '/student/exam/register/' + examId,
    method: 'post'
  })
}

// 开始考试
export function startExam(examId) {
  return request({
    url: '/student/exam/start/' + examId,
    method: 'post'
  })
}

// 获取考试试卷
export function getExamPaper(examId, examUserId) {
  return request({
    url: '/student/exam/paper/' + examId,
    method: 'get',
    params: examUserId ? { examUserId } : {}
  })
}

// 保存答题记录
export function saveAnswer(data) {
  return request({
    url: '/student/exam/answer',
    method: 'post',
    data: data,
    headers: {
      'repeatSubmit': false
    }
  })
}

// 提交考试
export function submitExam(examId, data) {
  return request({
    url: '/student/exam/submit/' + examId,
    method: 'post',
    data: data || {},
    headers: {
      'repeatSubmit': false
    }
  })
}

// 获取考试成绩
export function getExamResult(examId) {
  return request({
    url: '/student/exam/result/' + examId,
    method: 'get'
  })
}

// 获取考试剩余时间
export function getRemainingTime(examId, examUserId) {
  return request({
    url: '/student/exam/remainingTime/' + examId,
    method: 'get',
    params: examUserId ? { examUserId } : {}
  })
}

// 获取学员端统计数据
export function getDashboardStatistics() {
  return request({
    url: '/student/exam/statistics',
    method: 'get'
  })
}
