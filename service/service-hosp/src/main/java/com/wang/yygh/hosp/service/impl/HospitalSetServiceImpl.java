package com.wang.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.yygh.common.exception.YyghException;
import com.wang.yygh.common.result.ResultCodeEnum;
import com.wang.yygh.hosp.mapper.HospitalSetMapper;
import com.wang.yygh.hosp.service.HospitalSetService;
import com.wang.yygh.model.hosp.HospitalSet;
import com.wang.yygh.vo.order.SignInfoVo;
import org.springframework.stereotype.Service;
//  数据库 yygh_hosp
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {
    //  1、根据传递过来的医院编码，查询数据库，查询签名
    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(queryWrapper);
        return hospitalSet.getSignKey();
    }

    //TODO  2、遠程調用
    //获取医院签名信息
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        //  1、通過hoscode獲取醫院信息
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if(null == hospitalSet) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        //  2、獲取url和key
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }
}
