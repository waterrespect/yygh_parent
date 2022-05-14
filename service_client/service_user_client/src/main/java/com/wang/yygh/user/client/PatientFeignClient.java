package com.wang.yygh.user.client;

import com.wang.yygh.model.user.Patient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-user")
@Repository
public interface PatientFeignClient {

    @ApiOperation(value = "根據就診人id獲取就診人信息")
    @GetMapping("/api/user/patient/inner/get/{id}")
    public Patient getPatientOrder(@PathVariable("id") Long id);

}
