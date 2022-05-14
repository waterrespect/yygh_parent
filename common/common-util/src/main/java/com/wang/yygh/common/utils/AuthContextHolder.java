package com.wang.yygh.common.utils;

import com.wang.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

//TODO  獲取當前用戶信息工具類
public class AuthContextHolder {
    //  1、獲取當前用戶id
    public static Long getUserId(HttpServletRequest request) {
        //  1、從header中獲取token
        String token = request.getHeader("token");
        //  2、jwt從token中獲取user_id
        Long userId = JwtHelper.getUserId(token);
        System.out.println("userId => " + userId);
        return userId;
    }
    //  2、獲取當前用戶名稱
    public static String getUserName(HttpServletRequest request) {
        //  1、從header中獲取token
        String token = request.getHeader("token");
        //  2、jwt從token中獲取user_id
        String userName = JwtHelper.getUserName(token);
        System.out.println("username =>" + userName);
        return userName;
    }
}
