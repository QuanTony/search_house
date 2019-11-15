package com.project.search.controller;

import com.alibaba.fastjson.JSONObject;
import com.project.search.common.utils.*;
import com.project.search.common.validate.BeanValidators;
import com.project.search.config.interf.PassPermission;
import com.project.search.config.interf.RequiresPermission;
import com.project.search.constants.JwtConstants;
import com.project.search.dao.model.User;
import com.project.search.entity.param.LoginParam;
import com.project.search.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;


@Controller
public class LoginController {
    @Autowired
    UserService userService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 后台管理中心
     * @return
     */
    @GetMapping("/admin/center")
    public String adminCenterPage() {
        return "admin/center";
    }

    /**
     * 欢迎页
     * @return
     */
    @GetMapping("/admin/welcome")
    @PassPermission(value = "")
    public String welcomePage() {
        return "admin/welcome";
    }

    /**
     * 管理员登录页
     * @return
     */
    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }


    @ApiOperation(value="登录", notes="登录", produces = "application/json")
    @PostMapping("/login/admin")
    @ResponseBody
    public Object login(@RequestBody LoginParam loginParam){
        //js303校验
        BeanValidators.validate(loginParam);

        //查询是否有该用户
        if (!userService.checkUserExist(loginParam)){
            return new ResultHelper().newFailResult("用户名或密码错误");
        }

        //匹配用户密码与数据库是否匹配
        User user = userService.getUser(loginParam);
        if (!MD5Util.checkPwd(loginParam.getPassword(),user.getPassword(), String.valueOf(user.getId()))){
            return new ResultHelper().newFailResult("用户名或密码错误");
        }

        //创建jwt保存token,token保存到redis半个小时
        String userJson = JSONObject.toJSONString(user);
        String token = JwtUtil.createJWT(user.getId().toString(),userJson);
        redisUtil.set(JwtConstants.JWT_TOKEN_USER_ID + user.getId(),token,JwtConstants.JWT_EXPIRE_TIME);

        //TODO 目前没有前端吧token缓存到redis
//        redisUtil.set(String.valueOf(user.getId()),token);
        return new ResultHelper().newSuccessResult(token);
    }

    @PostMapping("/login/Authorization")
    @ResponseBody
    @RequiresPermission("role:admin")
    public Object adminAuthorization(){
        return new ResultHelper().newSuccessResult();
    }

    @PostMapping("/login/normalAuthorization")
    @ResponseBody
    @RequiresPermission("role:operator")
    public Object normalAuthorization(){
        return new ResultHelper().newSuccessResult();
    }
}
