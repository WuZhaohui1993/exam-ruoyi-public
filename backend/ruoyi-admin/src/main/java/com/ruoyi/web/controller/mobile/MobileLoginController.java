package com.ruoyi.web.controller.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.model.MobileLoginBody;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.system.service.ISysUserService;

import javax.annotation.Resource;

/**
 * 移动端登录Controller
 * 支持H5移动端通过身份证号+密码登录（无需验证码）
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/mobile")
public class MobileLoginController {
    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysLoginService loginService;

    /**
     * 移动端登录（身份证号+密码）
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody MobileLoginBody loginBody) {
        String idCard = loginBody.getIdCard();
        String password = loginBody.getPassword();

        // 参数校验
        if (StringUtils.isEmpty(idCard)) {
            return AjaxResult.error("身份证号不能为空");
        }
        if (StringUtils.isEmpty(password)) {
            return AjaxResult.error("密码不能为空");
        }

        // 通过身份证号查询用户
        SysUser user = userService.selectUserByIdCard(idCard);
        if (user == null) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(idCard, Constants.LOGIN_FAIL, "用户不存在"));
            return AjaxResult.error("用户不存在或身份证号错误");
        }

        // 检查用户状态
        if ("1".equals(user.getStatus())) {
            return AjaxResult.error("用户已被停用");
        }
        if ("2".equals(user.getDelFlag())) {
            return AjaxResult.error("用户已被删除");
        }

        // 使用用户名进行认证
        String username = user.getUserName();
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                    password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
                        MessageUtils.message("user.password.not.match")));
                return AjaxResult.error("密码错误");
            } else {
                AsyncManager.me()
                        .execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }

        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS,
                MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        loginService.recordLoginInfo(loginUser.getUserId());

        // 生成token
        String token = tokenService.createToken(loginUser);

        AjaxResult result = AjaxResult.success();
        result.put(Constants.TOKEN, token);
        result.put("user", user);

        // 如果传入了考试ID，则返回考试信息
        if (loginBody.getExamId() != null) {
            result.put("examId", loginBody.getExamId());
        }

        return result;
    }
}
