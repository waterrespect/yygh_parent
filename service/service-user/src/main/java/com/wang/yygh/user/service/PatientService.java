package com.wang.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.yygh.model.user.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {
    //  1、獲取就診人列表
    List<Patient> findAllUsrId(Long userId);
    //  3、根據id獲取就診人信息
    Patient getPatientById(Long id);
}
