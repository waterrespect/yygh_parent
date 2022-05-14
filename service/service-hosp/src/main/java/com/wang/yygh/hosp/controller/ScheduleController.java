package com.wang.yygh.hosp.controller;

import com.wang.yygh.common.result.Result;
import com.wang.yygh.hosp.service.ScheduleService;
import com.wang.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "排班管理")
@RestController
@RequestMapping("/admin/hosp/schedule")
//@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    //  1、根据医院编号、科室编号，查询排班规则数据
    @ApiOperation(value = "根据医院编号、科室编号，查询排班规则数据")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable long page,
                                  @PathVariable long limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.getScheduleRule(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    //  2、根据医院编号、科室比那好、工作日期，查询排班详细信息
    @ApiOperation(value = "根据医院编号、科室比那好、工作日期，查询排班详细信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable String hoscode,
                                    @PathVariable String depcode,
                                    @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return Result.ok(list);
    }
}
