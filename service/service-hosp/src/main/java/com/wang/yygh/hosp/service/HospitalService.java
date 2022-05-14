package com.wang.yygh.hosp.service;

import com.wang.yygh.model.hosp.Hospital;
import com.wang.yygh.vo.hosp.HospitalQueryVo;
import com.wang.yygh.vo.hosp.HospitalSetQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    //  1、上传医院接口
    void save(Map<String, Object> parameterMap);
    //  2、实现医院编号查询
    Hospital getByHoscode(String hoscode);
    //  3、查询医院（条件查询分页
    Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);
    //  4、修改医院状态
    void updateStatus(String id, Integer status);
    //  5、详细信息
    Map<String, Object> getHospById(String id);
    //  6、根据医院编号获取医院名称
    String getHospName(String hoscode);
    //  7、根据医院模糊查询
    List<Hospital> findByHosname(String hosname);
    //  8、根据医院编号获取医院详情信息
    Map<String, Object> item(String hoscode);
}
