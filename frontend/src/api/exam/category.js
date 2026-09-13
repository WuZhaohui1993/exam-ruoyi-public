import request from '@/utils/request'

// 查询试题分类列表
export function listCategory(query) {
  return request({
    url: '/exam/category/list',
    method: 'get',
    params: query
  })
}

// 查询试题分类下拉树
export function treeselect() {
  return request({
    url: '/exam/category/treeselect',
    method: 'get'
  })
}

// 查询试题分类详细
export function getCategory(id) {
  return request({
    url: '/exam/category/' + id,
    method: 'get'
  })
}

// 新增试题分类
export function addCategory(data) {
  return request({
    url: '/exam/category',
    method: 'post',
    data: data
  })
}

// 修改试题分类
export function updateCategory(data) {
  return request({
    url: '/exam/category',
    method: 'put',
    data: data
  })
}

// 删除试题分类
export function delCategory(id) {
  return request({
    url: '/exam/category/' + id,
    method: 'delete'
  })
}

// 导出试题分类
export function exportCategory(query) {
  return request({
    url: '/exam/category/export',
    method: 'post',
    params: query
  })
} 