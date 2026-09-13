import request from '@/utils/request'

// 查询考试分类列表
export function listExaminationCategory(query) {
  return request({
    url: '/exam/examination/category/list',
    method: 'get',
    params: query
  })
}

// 查询考试分类下拉树结构
export function treeselect() {
  return request({
    url: '/exam/examination/category/treeselect',
    method: 'get'
  })
}

// 根据角色ID查询考试分类树信息
export function roleExaminationCategoryTreeselect(roleId) {
  return request({
    url: '/exam/examination/category/roleExaminationCategoryTreeselect/' + roleId,
    method: 'get'
  })
}

// 查询考试分类详细
export function getExaminationCategory(id) {
  return request({
    url: '/exam/examination/category/' + id,
    method: 'get'
  })
}

// 新增考试分类
export function addExaminationCategory(data) {
  return request({
    url: '/exam/examination/category',
    method: 'post',
    data: data
  })
}

// 修改考试分类
export function updateExaminationCategory(data) {
  return request({
    url: '/exam/examination/category',
    method: 'put',
    data: data
  })
}

// 删除考试分类
export function delExaminationCategory(id) {
  return request({
    url: '/exam/examination/category/' + id,
    method: 'delete'
  })
}

// 校验考试分类名称
export function checkCategoryNameUnique(data) {
  return request({
    url: '/exam/examination/category/checkCategoryNameUnique',
    method: 'post',
    data: data
  })
} 