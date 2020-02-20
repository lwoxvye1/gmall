package com.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.UmsMember;
import com.gmall.service.UserService;
import com.gmall.util.HttpclientUtil;
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

    @RequestMapping("/vlogin")
    public String vlogin(String code, HttpServletRequest request){
        // 授权码换取access_token
        String s3 = "https://api.weibo.com/oauth2/access_token?";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "187638711");
        paramMap.put("client_secret", "a79777bba04ac70d973ee002d27ed58c");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://passport.gmall.com:8085/vlogin");
        paramMap.put("code", code);
        String access_token_json = HttpclientUtil.doPost(s3, paramMap);

        Map<String, Object> access_map = JSON.parseObject(
                access_token_json, Map.class);

        // access_token换取用户信息
        String uid = (String)access_map.get("uid");
        String access_token = (String)access_map.get("access_token");

        String showUserUrl = "https://api.weibo.com/2/users/show.json?access_token="
                + access_token + "&uid=" + uid;
        String userJson = HttpclientUtil.doGet(showUserUrl);
        Map<String, Object> userMap = JSON.parseObject(
                userJson, Map.class);

        // 将用户信息保存数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType(2);
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((Long)userMap.get("id"));
        umsMember.setCity((String)userMap.get("location"));
        String gender = (String) userMap.get("gender");
        int g = 0;
        if ("m".equals(gender)){
            g = 1;
        }
        umsMember.setGender(g);
        umsMember.setNickname((String)userMap.get("screen_name"));

        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userService.checkOauthUser(umsCheck);
        if (umsMemberCheck == null) {
            userService.addOauthUser(umsMember);
        } else {
            umsMember = umsMemberCheck;
        }

        // 生成token
        String token = "";
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        return "redirect:http://search.gmall.com:8083/index?token=" + token;
    }

}
