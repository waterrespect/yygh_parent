package com.wang.yygh.hosp.service;

import com.wang.yygh.model.hosp.Schedule;
import com.wang.yygh.vo.hosp.ScheduleOrderVo;
import com.wang.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    //  1、上传排班
    void save(Map<String, Object> parameterMap);
    //  2、查询排班
    Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);
    //  3、删除排班
    void remove(String hoscode, String hosScheduleId);
    //  4、根据医院编号,查询医院所有科室列表
    Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode);
    //  5、根据医院编号、科室比那好、工作日期，查询排班详细信息
    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);
    //  6、獲取可預約排版數據
    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);
    //  7、根據排班id獲取排版數據
    Schedule getById(String scheduleId);
    //  8、根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);
    //  9、更新排版數據,mq
    void update(Schedule schedule);
}
