package com.wang.yygh.user.api;

import com.wang.yygh.common.result.Result;
import com.wang.yygh.common.utils.AuthContextHolder;
import com.wang.yygh.model.user.Patient;
import com.wang.yygh.user.service.PatientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
    就診人接口
 */
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Autowired
    private PatientService patientService;
    //  1、獲取就診人列表
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = patientService.findAllUsrId(userId);
        return Result.ok(list);
    }
    //  2、添加就診人,即用戶
    @PostMapping("auth/save")
    public Result savepatient(@RequestBody Patient patient, HttpServletRequest request) {
        //  獲取當前就診人id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }
    //  3、根據id獲取就診人信息
    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }
    //  4、修改就診人
    @PutMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient) {
        //  獲取當前就診人id
        patientService.updateById(patient);
        return Result.ok();
    }
    //  5、刪除就診人
    @DeleteMapping("auth/remove/{id}")
    public Result deletePatient(@PathVariable Long id) {
        //  獲取當前就診人id
        patientService.removeById(id);
        return Result.ok();
    }

    //TODO 遠程調用接口
    @ApiOperation(value = "根據就診人id獲取就診人信息")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(@PathVariable("id") Long id) {
        return patientService.getPatientById(id);
    }

}
