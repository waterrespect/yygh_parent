package com.wang.yygh.hosp.controller;

import com.wang.yygh.common.result.Result;
import com.wang.yygh.hosp.service.HospitalService;
import com.wang.yygh.model.hosp.Hospital;
import com.wang.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "医院调用cmn字典")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    //  1、医院列表
    @ApiOperation(value = "医院列表")
    @GetMapping("list/{page}/{limit}")
    public Result listHosp(@PathVariable Integer page,
                           @PathVariable Integer limit,
                           HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> pageModel = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(pageModel);
    }
    //  2、修改医院状态
    @ApiOperation(value = "修改医院状态")
    @GetMapping("updateHospitalStatus/{id}/{status}")
    public Result updateHospitalStatus(@PathVariable String id,
                                       @PathVariable Integer status) {
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }
    //  3、详细信息
    @ApiOperation(value = "详细信息")
    @GetMapping("showHospital/{id}")
    public Result showHospital(@PathVariable String id) {
        Map<String, Object> map = hospitalService.getHospById(id);
        return Result.ok(map);
    }
}
