package com.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.R;
import com.reggie.entity.User;
import com.reggie.service.UserService;
import com.reggie.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * message verification
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //get phone number
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //create verification code 4 digits
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);
            //aliyun short message api to complete
            //SMSUtils.sendMessage("take out","",phone,code);

            //save code to session
            //session.setAttribute(phone,code);

            //save the code to redis and set the time
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("verification code sent successfully");
        }
        return R.error("failed sending verification code");
    }

    /**
     * font end user log in
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> lgoin(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //get phone number
        String phone = map.get("phone").toString();

        //verification code
        String code = map.get("code").toString();

        //get code from session
        //Object codeInSession = session.getAttribute(phone);

        //get code from redis
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //compare if code from session match the verificaiton code
        if(codeInSession != null && codeInSession.equals(code)){
            //if matching, login successfully
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if(user == null){
                //if phone number is new user, save info to user table
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        //if login successfully, del redis
        redisTemplate.delete(phone);

        return R.error("login failed");
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {
        //清理session中的用户id
        request.getSession().removeAttribute("user");
        return R.success("logged out successfully");
    }
}
