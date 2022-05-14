package com.wang.yygh.user.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConstantWxPropertiesUtils implements InitializingBean {

    @Value("${wx.open.app_id}")
    private String appId;
    @Value("${wx.open.app_secret}")
    private String appsecretId;
    @Value("${wx.open.redirect_ur1}")
    private String redirectUrl;
    @Value("${yygh.baseUrl}")
    private String yyghBaseUrl;

    public static String WX_OPEN_APP_ID;
    public static String WX_OPEN_APP_SECRET;
    public static String WX_OPEN_REDIRECT_URL;
    public static String YYGH_BASE_URL;

    @Override
    public void afterPropertiesSet() throws Exception {
        WX_OPEN_APP_ID = appId;
        WX_OPEN_APP_SECRET = appsecretId;
        WX_OPEN_REDIRECT_URL = redirectUrl;
        YYGH_BASE_URL = yyghBaseUrl;
    }
}
