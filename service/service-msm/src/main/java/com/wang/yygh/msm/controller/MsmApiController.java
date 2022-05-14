package com.wang.yygh.msm.controller;

import com.wang.yygh.common.result.Result;
import com.wang.yygh.msm.service.MsmService;
import com.wang.yygh.msm.utils.RandomUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
//@CrossOrigin
public class MsmApiController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @ApiOperation(value = "发送手机验证码")
    @GetMapping("send/{phone}")
    public Result sendCode(@PathVariable String phone) {
        //  1、从redis取验证码
        //  key 手机号 => value 验证那个干嘛
        String code = redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)) {
            return Result.ok(code);
        }
        //  2、获取不到=>生成验证码,通过整合短信服务发送,验证码放置到redis，并设置有效时间
        //  生成验证码
        code =RandomUtil.getSixBitRandom();
        //
        boolean isSend = msmService.send(phone, code);
        //  生成验证码，放到redis中
        if(isSend) {
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return Result.ok();
        } else {
            return Result.fail().message("发送短信失败");
        }
    }
}
