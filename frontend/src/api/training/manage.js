import request from '@/utils/request'

export function listTrainingItem(query) {
  return request({
    url: '/training/manage/list',
    method: 'get',
    params: query
  })
}

export function listTrainingLearningLedger(query) {
  return request({
    url: '/training/manage/learning-ledger',
    method: 'get',
    params: query
  })
}

export function getTrainingItem(id) {
  return request({
    url: '/training/manage/' + id,
    method: 'get'
  })
}

export function addTrainingItem(data) {
  return request({
    url: '/training/manage',
    method: 'post',
    data
  })
}

export function updateTrainingItem(data) {
  return request({
    url: '/training/manage',
    method: 'put',
    data
  })
}

export function delTrainingItem(ids) {
  return request({
    url: '/training/manage/' + ids,
    method: 'delete'
  })
}

export function getTrainingSummary() {
  return request({
    url: '/training/manage/summary',
    method: 'get'
  })
}

export function listTrainingCourseOptions(keyword) {
  return request({
    url: '/training/manage/options/courses',
    method: 'get',
    params: { keyword }
  })
}

export function listTrainingExamOptions(keyword) {
  return request({
    url: '/training/manage/options/exams',
    method: 'get',
    params: { keyword }
  })
}

export function listTrainingStudentOptions(keyword) {
  return request({
    url: '/training/manage/options/students',
    method: 'get',
    params: { keyword }
  })
}

export function replyTrainingQuestion(id, data) {
  return request({
    url: `/training/manage/qa/${id}/reply`,
    method: 'put',
    data
  })
}

export function issueCertificate(data) {
  return request({
    url: '/training/manage/certificate',
    method: 'post',
    data
  })
}

export function processMediaResource(id) {
  return request({
    url: `/training/manage/media/${id}/process`,
    method: 'post'
  })
}

export function listTrainingAttachment(query) {
  return request({
    url: '/training/manage/attachment/list',
    method: 'get',
    params: query
  })
}

export function delTrainingAttachment(ids) {
  return request({
    url: '/training/manage/attachment/' + ids,
    method: 'delete'
  })
}
