package com.wang.yygh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.yygh.common.result.Result;
import com.wang.yygh.model.user.UserInfo;
import com.wang.yygh.user.service.UserInfoService;
import com.wang.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api("平臺用戶管理")
@RestController
@RequestMapping("/admin/user")
public class UserController {
    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "用戶列表,帶分頁查詢")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                        UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> pageParam = new Page<>(page, limit);
        IPage<UserInfo> pageModel
                = userInfoService.selectPage(pageParam, userInfoQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "用戶鎖定")
    @GetMapping("lock/{userId}/{status}")
    public Result lock(@PathVariable Long userId,
                       @PathVariable Integer status) {
        userInfoService.lock(userId, status);
        return Result.ok();
    }

    @ApiOperation(value = "用戶詳情")
    @GetMapping("show/{userId}")
    public Result show(@PathVariable Long userId) {
        Map<String, Object> map = userInfoService.show(userId);
        return Result.ok(map);
    }

    @ApiOperation(value = "認證審批")
    @GetMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,
                           @PathVariable Integer authStatus) {
        userInfoService.approval(userId, authStatus);
        return Result.ok();
    }
}
