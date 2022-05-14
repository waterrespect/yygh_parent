package com.wang.yygh.msm.service;

import com.wang.yygh.vo.msm.MsmVo;

public interface MsmService {
    //  1、发送手机验证码
    boolean send(String phone, String code);
    //  2、MQ使用發送短信
    boolean send(MsmVo msmVo);
}
