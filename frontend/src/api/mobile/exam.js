import request from '@/utils/request'

/**
 * H5移动端考试API
 */

// 获取考试信息
export function getExamInfo(examId) {
    return request({
        url: '/mobile/exam/' + examId + '/info',
        method: 'get'
    })
}

// 开始考试
export function startExam(examId) {
    return request({
        url: '/mobile/exam/' + examId + '/start',
        method: 'post'
    })
}

// 获取考试试卷
export function getExamPaper(examId, examUserId) {
    return request({
        url: '/mobile/exam/' + examId + '/paper',
        method: 'get',
        params: examUserId ? { examUserId } : {}
    })
}

// 保存答案
export function saveAnswer(data) {
    return request({
        url: '/mobile/exam/answer',
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
        url: '/mobile/exam/' + examId + '/submit',
        method: 'post',
        data: data || {},
        headers: {
            'repeatSubmit': false
        }
    })
}

// 获取考试结果
export function getExamResult(examId, examUserId) {
    return request({
        url: '/mobile/exam/' + examId + '/result',
        method: 'get',
        params: examUserId ? { examUserId } : {}
    })
}

// 上传考试附件记录
export function addExamAttachment(data) {
    return request({
        url: '/student/training/attachment',
        method: 'post',
        data
    })
}
