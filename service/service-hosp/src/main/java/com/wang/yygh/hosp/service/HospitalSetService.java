package com.wang.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.yygh.model.hosp.HospitalSet;
import com.wang.yygh.vo.order.SignInfoVo;

public interface HospitalSetService extends IService<HospitalSet> {
    //  根据传递过来的医院编码，查询数据库，查询签名
    String getSignKey(String hoscode);


    //TODO  2、遠程調用
    //获取医院签名信息
    SignInfoVo getSignInfoVo(String hoscode);
}
