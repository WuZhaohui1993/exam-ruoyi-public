import request from '@/utils/request'

export function getStudentTrainingSummary() {
  return request({
    url: '/student/training/summary',
    method: 'get'
  })
}

export function getStudentCourses() {
  return request({
    url: '/student/training/courses',
    method: 'get'
  })
}

export function getStudentCourseItems(courseId) {
  return request({
    url: `/student/training/course/${courseId}/items`,
    method: 'get'
  })
}

export function getStudentCourseProgress(courseId) {
  return request({
    url: `/student/training/course/${courseId}/progress`,
    method: 'get'
  })
}

export function getStudentResources(query) {
  return request({
    url: '/student/training/resources',
    method: 'get',
    params: query
  })
}

export function getStudentCertificates() {
  return request({
    url: '/student/training/certificates',
    method: 'get'
  })
}

export function listStudentQa(query) {
  return request({
    url: '/student/training/qa',
    method: 'get',
    params: query
  })
}

export function markCourseProgress(courseId, data) {
  return request({
    url: `/student/training/course/${courseId}/progress`,
    method: 'post',
    data
  })
}

export function startCourseExam(courseId) {
  return request({
    url: `/student/training/course/${courseId}/exam/start`,
    method: 'post'
  })
}

export function recordPractice(data) {
  return request({
    url: '/student/training/practice',
    method: 'post',
    data
  })
}

export function listStudentPractice(query) {
  return request({
    url: '/student/training/practice/list',
    method: 'get',
    params: query
  })
}

export function askTrainingQuestion(data) {
  return request({
    url: '/student/training/qa',
    method: 'post',
    data
  })
}

export function listStudentAttachment(query) {
  return request({
    url: '/student/training/attachment/list',
    method: 'get',
    params: query
  })
}

export function addStudentAttachment(data) {
  return request({
    url: '/student/training/attachment',
    method: 'post',
    data
  })
}
