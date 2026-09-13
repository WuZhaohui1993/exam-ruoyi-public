import request from '@/utils/request'

// 查询试卷分类列表
export function listCategory(query) {
  return request({
    url: '/exam/paper/category/list',
    method: 'get',
    params: query
  })
}

// 查询试卷分类详细
export function getCategory(id) {
  return request({
    url: '/exam/paper/category/' + id,
    method: 'get'
  })
}

// 新增试卷分类
export function addCategory(data) {
  return request({
    url: '/exam/paper/category',
    method: 'post',
    data: data
  })
}

// 修改试卷分类
export function updateCategory(data) {
  return request({
    url: '/exam/paper/category',
    method: 'put',
    data: data
  })
}

// 删除试卷分类
export function delCategory(id) {
  return request({
    url: '/exam/paper/category/' + id,
    method: 'delete'
  })
}

// 查询试卷分类下拉树结构
export function categoryTreeselect() {
  return request({
    url: '/exam/paper/category/treeselect',
    method: 'get'
  })
} 