package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        String subject = "瑞吉餐购登录验证码";
        if(StringUtils.isNotEmpty(phone)){
            //生成随机四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            String context = "欢迎使用瑞吉餐购,登录验证码为:"+code+",五分钟有效,请妥善保管!";
            log.info("code = {}",code);
            //调用邮箱服务
            userService.sendMsg(phone,subject,context);
            session.setAttribute(phone,code);
            return R.success("邮箱登录验证码发送成功");
        }
        return R.error("邮箱登录验证码发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        //从map中获取手机号(用户提交)
        String phone = map.get("phone").toString();
        //从map中获取验证码(用户提交)
        String code = map.get("code").toString();
        //从session中获取保存的验证码(服务器生成)
        Object codeInSession = session.getAttribute(phone);
        //进行验证码比对
        if(codeInSession!=null && codeInSession.equals(code)){
            //比对成功,则登录成功
            //判断当前手机号用户是否为新用户,如果是新用户 则自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user==null){//是新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());//由mybatisPlus使用雪花算法自动生成用户id自动生成
            return R.success(user);
        }
        return R.error("验证码有误");
    }

    /**
     * 移动端用户退出登录
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginout(){
        //清理session中保存的当前登录员工的id
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
