package com.wang.yygh.hosp.repository;

import com.wang.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    //  1、判断是否存在数据
    Hospital getHospitalByHoscode(String hoscode);
    //  2、根据医院模糊查询
    List<Hospital> findHospitalByHosnameLike(String hosname);
}
