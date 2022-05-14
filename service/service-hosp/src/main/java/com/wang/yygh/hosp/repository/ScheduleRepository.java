package com.wang.yygh.hosp.repository;

import com.wang.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    //  1、获得排班
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);
    //  5、根据医院编号、科室比那好、工作日期，查询排班详细信息
    List<Schedule> findSchduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date toDate);
}
