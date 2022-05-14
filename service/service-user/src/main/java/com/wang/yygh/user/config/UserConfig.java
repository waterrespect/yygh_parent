package com.wang.yygh.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.wang.yygh.user.mapper.")
public class UserConfig {
}
