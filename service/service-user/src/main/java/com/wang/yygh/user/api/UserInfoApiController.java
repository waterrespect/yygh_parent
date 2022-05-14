package com.wang.yygh.user.api;

import com.wang.yygh.common.result.Result;
import com.wang.yygh.common.utils.AuthContextHolder;
import com.wang.yygh.model.user.UserInfo;
import com.wang.yygh.user.service.UserInfoService;
import com.wang.yygh.vo.user.LoginVo;
import com.wang.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "用户登录")
@RestController
@RequestMapping("/api/user")
//@CrossOrigin
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    //  1、用户手机号登录接口
    @ApiOperation(value = "用户手机号登录接口")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        Map<String, Object> info = userInfoService.loginUser(loginVo);
        return Result.ok(info);
    }
    //  2、用戶認證
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //  token中用戶id，用戶userAuthVo認證信息
        userInfoService.userAuth(AuthContextHolder.getUserId(request), userAuthVo);
        return Result.ok();
    }
    //  3、獲取用戶id信息
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }
}
