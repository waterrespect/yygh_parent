package com.wang.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.yygh.model.user.UserInfo;
import com.wang.yygh.vo.user.LoginVo;
import com.wang.yygh.vo.user.UserAuthVo;
import com.wang.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    //TODO 前端用戶管理

    //  1、用户手机号登录接口
    Map<String, Object> loginUser(LoginVo loginVo);
    //  2、查找openid是否已經存在
    UserInfo selecWXInfoOpenId(String openid);
    //  3、用戶認證
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //TODO 平臺用戶管理

    //  1、用戶列表,帶分頁查詢
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);
    //  2、用戶鎖定
    void lock(Long userId, Integer status);
    //  3、用戶詳情
    Map<String, Object> show(Long userId);
    //  4、認證審批
    void approval(Long userId, Integer authStatus);
}
