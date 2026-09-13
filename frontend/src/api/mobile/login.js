import request from '@/utils/request'

/**
 * H5移动端登录API
 */

// 移动端登录（身份证号+密码）
export function mobileLogin(idCard, password, examId) {
    const data = {
        idCard,
        password,
        examId
    }
    return request({
        url: '/mobile/login',
        headers: {
            isToken: false,
            repeatSubmit: false
        },
        method: 'post',
        data: data
    })
}
