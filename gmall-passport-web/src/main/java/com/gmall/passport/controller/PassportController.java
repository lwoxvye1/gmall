package com.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.UmsMember;
import com.gmall.service.UserService;
import com.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
    @Reference
    UserService userService;

    @RequestMapping("/index")
    public String index(String ReturnUrl, ModelMap map){
        map.put("ReturnUrl", ReturnUrl);
        return "index";
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        String token = "";
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin != null){
            // jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", memberId);
            userMap.put("nickname", nickname);

            // 通过nginx转发的客户端IP
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)){
                // 从request中获取ip
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    // 异常处理
                }
            }

            token = JwtUtil.encode("2019gmall0219", userMap, ip);

            // 将token存入redis一份
            userService.addUserToken(token, memberId);
        } else {
            token = "fail";
        }

        return token;
    }

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token, String currentIp){
        Map<String, String> map = new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(
                token, "2019gmall0219", currentIp);

        if (decode != null) {
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));
        } else {
            map.put("status", "fail");
        }
        return JSON.toJSONString(map);
    }
}
