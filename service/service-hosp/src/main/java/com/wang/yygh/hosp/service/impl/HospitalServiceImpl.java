package com.wang.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wang.yygh.cmn.client.DictFeignClient;
import com.wang.yygh.hosp.repository.HospitalRepository;
import com.wang.yygh.hosp.service.HospitalService;
import com.wang.yygh.model.hosp.Hospital;
import com.wang.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

//  mongodb hospital
@Service
public class HospitalServiceImpl implements HospitalService {
    //  mongoDb
    @Autowired
    private HospitalRepository hospitalRepository;
    //  cmn方法
    @Autowired
    private DictFeignClient dictFeignClient;

    //  1、上传医院接口
    @Override
    public void save(Map<String, Object> parameterMap) {
        //  1、将map转换为 hospital 类
        //  fast json
        String mapString = JSONObject.toJSONString(parameterMap);
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);
        //  2、判断是否存在数据
        String hosCode = hospital.getHoscode();
        Hospital hospitalExist =  hospitalRepository.getHospitalByHoscode(hosCode);
        //  存在
        if(hospitalExist != null) {
            //  状态
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            //  方法，插入
            hospitalRepository.save(hospital);
        } else {
            //  不存在
            //  状态
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            //  方法，插入
            hospitalRepository.save(hospital);
        }
    }
    //  2、实现医院编号查询
    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }
    //  3、查询医院（条件查询分页
    @Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //  1、创建pageabld
        Pageable pageable = PageRequest.of(page - 1, limit);
        //  2、条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //  3、具体对象
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        //  4、创建对象
        Example<Hospital> example = Example.of(hospital, matcher);
        //  5、调用方法实现查询
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);
        //  6、远程调用cmn方法查询dict医院分类名字
//        List<Hospital> content = pages.getContent();
        //  厘面有param可以传入医院等级
        //  获取查询list集合，遍历进行医院等级封装
        pages.getContent().stream().forEach(item -> {
            this.setHospitalHosType(item);
        });
        return pages;
    }

    //  获取查询list集合，遍历进行医院等级封装
    private Hospital setHospitalHosType(Hospital hospital) {
        //  dictCode 是医院类型dict
        //  在mongodb中的hosp hostype == value
        //  根据dictCode和value获取医院等级名称
        //  这个hospital是mongodb中的hospital
        String hostypeString = dictFeignClient.getName("Hostype", hospital.getHostype());
        //  查询省 市 地区
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String CityString = dictFeignClient.getName(hospital.getCityCode());
        String DistrictString = dictFeignClient.getName(hospital.getDistrictCode());
        //  将获得的type_String给hospital_map中,返回
        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress", provinceString + CityString + DistrictString);
        return hospital;
    }

    //  4、修改医院状态
    @Override
    public void updateStatus(String id, Integer status) {
        //  1、根据id查询医院信息
        Hospital hospital = hospitalRepository.findById(id).get();
        //  2、设置修改的值
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }
    //  5、详细信息
    @Override
    public Map<String, Object> getHospById(String id) {
        //  获取查询list集合，遍历进行医院等级封装
        Hospital hospital = hospitalRepository.findById(id).get();
        Hospital hospital_package = this.setHospitalHosType(hospital);
        //  放到map中
        Map<String, Object> result = new HashMap<>();
        //  包含基本信息，医院等级等
        result.put("hospital", hospital_package);
        result.put("bookingRule", hospital_package.getBookingRule());
        //  不需要重复返回
        hospital_package.setBookingRule(null);
        return result;
    }
    //  6、根据医院编号获取医院名称
    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospital != null) {
            return hospital.getHosname();
        }
        return null;
    }
    //  7、根据医院模糊查询
    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }
    //  8、根据医院编号获取医院详情信息
    @Override
    public Map<String, Object> item(String hoscode) {
        Map<String, Object> map = new HashMap<>();
        //  1、获取医院信息
        Hospital hospital = this.setHospitalHosType(this.getByHoscode(hoscode));
        map.put("hospital", hospital);
        //  2、获取挂号规则
        map.put("bookingRule", hospital.getBookingRule());
        //  3、不重复返回
        hospital.setBookingRule(null);
        return map;
    }
}
