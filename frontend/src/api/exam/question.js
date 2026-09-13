import request from '@/utils/request'

// 查询试题列表
export function listQuestion(query) {
  return request({
    url: '/exam/question/list',
    method: 'get',
    params: query
  })
}

// 查询试题详细
export function getQuestion(id) {
  return request({
    url: '/exam/question/' + id,
    method: 'get'
  })
}

// 预览试题
export function previewQuestion(id) {
  return request({
    url: '/exam/question/preview/' + id,
    method: 'get'
  })
}

// 新增试题
export function addQuestion(data) {
  return request({
    url: '/exam/question',
    method: 'post',
    data: data
  })
}

// 修改试题
export function updateQuestion(data) {
  return request({
    url: '/exam/question',
    method: 'put',
    data: data
  })
}

// 删除试题
export function delQuestion(id) {
  return request({
    url: '/exam/question/' + id,
    method: 'delete'
  })
}

// 导出试题
export function exportQuestion(query) {
  return request({
    url: '/exam/question/export',
    method: 'post',
    params: query
  })
}

// 获取试题分类列表
export function getCategoryList() {
  return request({
    url: '/exam/question/categoryList',
    method: 'get'
  })
}

// 根据分类ID获取试题
export function getQuestionsByCategory(categoryId) {
  return request({
    url: '/exam/question/category/' + categoryId,
    method: 'get'
  })
}

// 根据题型获取试题
export function getQuestionsByType(questionType) {
  return request({
    url: '/exam/question/type/' + questionType,
    method: 'get'
  })
}

// 根据难度等级获取试题
export function getQuestionsByDifficulty(difficultyLevel) {
  return request({
    url: '/exam/question/difficulty/' + difficultyLevel,
    method: 'get'
  })
}

// 根据标签获取试题
export function getQuestionsByTags(tags) {
  return request({
    url: '/exam/question/tags/' + tags,
    method: 'get'
  })
}

// 统计各题型试题数量
export function getQuestionTypeStatistics() {
  return request({
    url: '/exam/question/statistics/type',
    method: 'get'
  })
}

// 统计试题数量
export function countQuestions(query) {
  return request({
    url: '/exam/question/count',
    method: 'get',
    params: query
  })
} 