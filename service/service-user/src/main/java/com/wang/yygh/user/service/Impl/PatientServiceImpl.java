package com.wang.yygh.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.yygh.cmn.client.DictFeignClient;
import com.wang.yygh.enums.DictEnum;
import com.wang.yygh.model.user.Patient;
import com.wang.yygh.user.mapper.PatientMapper;
import com.wang.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {
    //  cmn方法
    @Autowired
    private DictFeignClient dictFeignClient;

    //  1、獲取就診人列表
    @Override
    public List<Patient> findAllUsrId(Long userId) {
        //  1、查找
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Patient> patientList = baseMapper.selectList(queryWrapper);
        //  2、查找其他信息，通過編號或編碼獲取信息
        patientList.stream().forEach(item -> {
            this.packPatient(item);
        });
        return patientList;
    }

    //  3、根據id獲取就診人信息
    @Override
    public Patient getPatientById(Long id) {
        Patient patient = baseMapper.selectById(id);
        // 查找其他信息，通過編號或編碼獲取信息
        this.packPatient(patient);
        return patient;
    }

    //TODO 查詢數據字典表内容
    private Patient packPatient(Patient item) {
        //  1、查詢證件類型名稱
        String CertificatesTypeName =
                dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), item.getCertificatesType());
        //  2、查詢联系人证件类型
        System.out.println("getContactsCertificatesType =>" + item.getContactsCertificatesType());
        if(!item.getContactsCertificatesType().equals("")) {
            String ContactsCertificatesTypeName =
                    dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), item.getContactsCertificatesType());
            item.getParam().put("contactsCertificatesTypeString", ContactsCertificatesTypeName);
        }
        //  3、查詢省code districtCode
        String ProvinceName = dictFeignClient.getName(item.getProvinceCode());
        //  4、市code
        String CityName = dictFeignClient.getName(item.getCityCode());
        //  5、区code
        String DistrictName = dictFeignClient.getName(item.getDistrictCode());

        item.getParam().put("certificatesTypeString", CertificatesTypeName);

        item.getParam().put("provinceString", ProvinceName);
        item.getParam().put("cityString", CityName);
        item.getParam().put("districtString", DistrictName);
        item.getParam().put("fullAddress", ProvinceName + CityName + DistrictName + item.getAddress());

        return item;
    }
}
