package com.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.gmall.annotations.LoginRequired;
import com.gmall.util.CookieUtil;
import com.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(
                LoginRequired.class);
        if (methodAnnotation == null) {
            return true;
        }

        String token = "";
        String oldToken = CookieUtil.getCookieValue(
                request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        // 获得该请求是否必须登录
        boolean loginSuccess = methodAnnotation.loginSuccess();

        // 调用认证中心进行验证
        String success = "fail";
        Map<String, String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)) {
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)){
                // 从request中获取ip
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    // 异常处理
                }
            }
            // 将客户端的ip传过去，因为此次请求是由拦截器发起的，所以verify
            // 方法得不到客户端的ip
             String successJson = HttpclientUtil.doGet(
                     "http://passport.gmall.com:8085/verify?token="
                             + token + "&currentIp" + ip);
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");
        }

        if (loginSuccess) {
            // 必须登录成功才能访问
            if (!"success".equals(success)) {
                // 重定向回passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect(
                        "http://passport.gmall.com:8085/index?ReturnUrl" + requestURL);
                return false;
            }
        } else {
            // 没有登录也能用，但是必须验证
            if (!"success".equals(success)) {
                return true;
            }
        }

        // success, 跟新cookie及添加属性
        request.setAttribute("memberId", successMap.get("memberId"));
        request.setAttribute("nickname", successMap.get("nickname"));
        CookieUtil.setCookie(request, response, "oldToken",
                token, 60 * 60 * 2, true);
        return true;
    }
}
