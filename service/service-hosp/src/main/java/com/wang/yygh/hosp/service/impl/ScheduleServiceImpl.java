package com.wang.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.yygh.common.exception.YyghException;
import com.wang.yygh.common.result.ResultCodeEnum;
import com.wang.yygh.hosp.mapper.ScheduleMapper;
import com.wang.yygh.hosp.repository.ScheduleRepository;
import com.wang.yygh.hosp.service.DepartmentService;
import com.wang.yygh.hosp.service.HospitalService;
import com.wang.yygh.hosp.service.ScheduleService;
import com.wang.yygh.model.hosp.BookingRule;
import com.wang.yygh.model.hosp.Department;
import com.wang.yygh.model.hosp.Hospital;
import com.wang.yygh.model.hosp.Schedule;
import com.wang.yygh.vo.hosp.BookingScheduleRuleVo;
import com.wang.yygh.vo.hosp.ScheduleOrderVo;
import com.wang.yygh.vo.hosp.ScheduleQueryVo;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> parameterMap) {
        //  1、转换成Schedule对象
        String parameterMapString = JSONObject.toJSONString(parameterMap);
        Schedule schedule = JSONObject.parseObject(parameterMapString, Schedule.class);
        //  2、判断是否存在
        Schedule scheduleExist =scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        //  3、判断
        if(scheduleExist != null) {
            scheduleExist.setUpdateTime(new Date());
            scheduleExist.setIsDeleted(0);
            scheduleExist.setStatus(1);
            scheduleRepository.save(scheduleExist);
        } else {
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }
    }
    //  2、查询排班
    @Override
    public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        //  1、创建pageable对象,设置当前页和每页记录数
        Pageable pageable = PageRequest.of(page - 1, limit);
        //  2、创建Example对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //  3、转换
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);

        Example<Schedule> example = Example.of(schedule, matcher);
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }
    //  3、删除排班
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //  1、根据医院id和排班id查询信息
        Schedule sch = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        //  2、判断
        if(null != sch) {
            scheduleRepository.deleteById(sch.getId());
        }
    }
    //  4、根据医院编号,查询医院所有科室列表
    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
        //  1、根据医院编号，科室编号查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //  2、根据工作日期work date分组
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//  匹配条件
                Aggregation.group("workDate")//分组字段
                .first("workDate").as("workDate")
                //  3、统计号源数量
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //  排序
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                //  4、实现分页
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)

        );
        //  3、调用方法，实现查询
        AggregationResults<BookingScheduleRuleVo> aggResult =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        //  实现根据医院编号、科室编号查询，通过工作日期分组，号源统计，日期排序，分页后的数据
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResult.getMappedResults();

        //  4、分组查询的总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResult =
                mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResult.getMappedResults().size();

        //  5、处理日期为星期
        for(BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //  6、设置最终数据，返回
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleVoList", bookingScheduleRuleVoList);
        result.put("total", total);

        //  7、获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        //  其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hosName);
        result.put("baseMap", baseMap);

        return result;
    }

    //  日期改星期
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
                break;
            default:
                break;
        }
        return dayOfWeek;
    }
    //  5、根据医院编号、科室比那好、工作日期，查询排班详细信息
    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        //  1、查询mongodb
        List<Schedule> scheduleList =
                scheduleRepository.findSchduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());

        //  2、遍历，获取其他值,医院名称，科室名称，日期对应星期
        scheduleList.stream().forEach(item -> {
            this.packageSchedule(item);
        });
        return scheduleList;
    }


    //  6、封装其他数据
    private Schedule packageSchedule(Schedule schedule) {
        //  1、医院名称
        schedule.getParam().put("hosname", hospitalService.getHospName(schedule.getHoscode()));
        //  2、科室名称
        schedule.getParam().put("depname", departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        //  3、日期对应星期
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }

    //TODO 獲取可預約排版數據
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //TODO  1、返回map
        Map<String, Object> result = new HashMap<>();
        //TODO  2、根據預約編號獲取預約規則
        //  根據醫院編號獲取預約規則
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if(null == hospital) throw new YyghException(ResultCodeEnum.DATA_ERROR);
        BookingRule bookingRule = hospital.getBookingRule();
        //TODO  獲取可獲取日期數據，分頁
        IPage iPage = this.getListDate(page, limit, bookingRule);
        //  當前可預約日期
        List<Date> dateList = iPage.getRecords();

        //TODO 獲取可預約日期裏面科室的剩餘預約數
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode)
                .and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                .count().as("docCount")
                .sum("availableNumber").as("availableNumber")// 剩余预约数
                .sum("reservedNumber").as("reservedNumber")// 可预约数
        );
        //  執行
        AggregationResults<BookingScheduleRuleVo> aggregateResult
                = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregateResult.getMappedResults();

        //TODO  數據合并
        // key:日期, value : 預約規則和剩餘數量
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream().collect(
                    Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                            BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //TODO  獲取可預約排班規則
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0; i < dateList.size(); i++) {
            Date date = dateList.get(i);
            //  從map集合中根據key日期獲取value值()
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            //  如果當天沒有排班醫生
            if(null == bookingScheduleRuleVo) {
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //  就診醫生人數
                bookingScheduleRuleVo.setDocCount(0);
                //  科室剩餘預約數, -1表示無號
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //  計算當前預約日期對應星期
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            // 最后一页 最后一条记录 为即将预约
            // 状态 0：正常 1：即将放号 -1：当天已停止挂号
            if(i == dateList.size()-1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            //TODO  添加排班規則到list中
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //TODO 可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //TODO 其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);

        return result;
    }

    //TODO  獲取可獲取日期數據，分頁 => 10天循環，
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //  1、獲取當天放號時間,年、月、日、小時、分鐘
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //  2、預約周期
        Integer cycle = bookingRule.getCycle();
        //  3、如果當天時間已經過去了，預約周期從後一天開始計算,周期+1day
        if (releaseTime.isBeforeNow()) cycle += 1;
        //  4、獲取可預約的所有日期，最後一天即將放號
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateTime = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateTime).toDate());
        }
        //  5、分頁, limit 7天
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        //  判斷 : 如果小於7，直接顯示
        if(end > dateList.size()) {
            end = dateList.size();
        }
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        //  分頁
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, 7, dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }
    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    //  7、根據排班id獲取排版數據
    @Override
    public Schedule getById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        return this.packageSchedule(schedule);
    }

    //  8、根据排班id获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //  排班信息
        Schedule schedule = this.getById(scheduleId);
        if(null == schedule) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //  預約規則
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(null == hospital) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //  封裝 scheduleOrderVo
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        //  日期處理
        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        return scheduleOrderVo;
    }

    //  9、更新排版數據,mq
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

}
