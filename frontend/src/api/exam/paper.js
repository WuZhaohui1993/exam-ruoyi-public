import request from '@/utils/request'

// 查询试卷列表
export function listPaper(query) {
  return request({
    url: '/exam/paper/list',
    method: 'get',
    params: query
  })
}

// 查询试卷详细
export function getPaper(id) {
  return request({
    url: '/exam/paper/' + id,
    method: 'get'
  })
}

// 新增试卷
export function addPaper(data) {
  return request({
    url: '/exam/paper',
    method: 'post',
    data: data
  })
}

// 修改试卷
export function updatePaper(data) {
  return request({
    url: '/exam/paper',
    method: 'put',
    data: data
  })
}

// 修改试卷（包含题目）
export function updatePaperFull(data) {
  return request({
    url: '/exam/paper/full',
    method: 'put',
    data: data
  })
}

// 删除试卷
export function delPaper(id) {
  return request({
    url: '/exam/paper/' + id,
    method: 'delete'
  })
}

// 复制试卷
export function copyPaper(id) {
  return request({
    url: '/exam/paper/copy/' + id,
    method: 'post'
  })
}

// 预览试卷
export function previewPaper(id) {
  return request({
    url: '/exam/paper/preview/' + id,
    method: 'get'
  })
}

// 创建固定试卷
export function createFixedPaper(data) {
  return request({
    url: '/exam/paper/fixed',
    method: 'post',
    data: data
  })
}

// 创建随机试卷
export function createRandomPaper(data) {
  return request({
    url: '/exam/paper/random',
    method: 'post',
    data: data
  })
}

// 创建混合试卷
export function createMixedPaper(data) {
  return request({
    url: '/exam/paper/mixed',
    method: 'post',
    data: data
  })
}

// 生成试卷题目（随机试卷）
export function generatePaperQuestions(paperId) {
  return request({
    url: '/exam/paper/' + paperId + '/generate',
    method: 'get'
  })
}

// 获取试卷题目列表
export function getPaperQuestions(paperId) {
  return request({
    url: '/exam/paper/' + paperId + '/questions',
    method: 'get'
  })
}

// 根据分类查询试卷
export function getPapersByCategory(categoryId) {
  return request({
    url: '/exam/paper/list/category/' + categoryId,
    method: 'get'
  })
}

// 根据类型查询试卷
export function getPapersByType(paperType) {
  return request({
    url: '/exam/paper/type/' + paperType,
    method: 'get'
  })
} 