package com.wang.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.util.StringUtils;
import com.wang.yygh.common.config.utils.MD5;
import com.wang.yygh.common.exception.YyghException;
import com.wang.yygh.common.result.Result;
import com.wang.yygh.hosp.service.HospitalSetService;
import com.wang.yygh.model.hosp.HospitalSet;
import com.wang.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
//@CrossOrigin
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    //  1、查询
    @ApiOperation(value = "获取所有医院设置")
    @GetMapping("findAll")
    public Result findAllHospitalSet() {
        //调用service的方法
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    //  2、逻辑删除医院设置
    @ApiOperation(value = "逻辑删除医院设置")
    @DeleteMapping("{id}")
    public Result removeHospSet(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if(flag == true) {
            return Result.ok();
        }
        return Result.fail();
    }
    //  3、条件查询带分页
    @ApiOperation(value = "条件查询带分页")
    @PostMapping("findPage/{current}/{limit}")
    public Result findPageHospSet(@PathVariable long current,
                                  @PathVariable long limit,
                                  @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        //  创建page对象，传递当前页，每页记录数
        Page<HospitalSet> page = new Page<>(current, limit);
        //  条件构造
        String hosname = hospitalSetQueryVo.getHosname();
        String hoscode = hospitalSetQueryVo.getHoscode();
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        if(!StringUtils.isNullOrEmpty(hosname)) {
            wrapper.like("hosname", hosname);
        }
        if(!StringUtils.isNullOrEmpty(hoscode)) {
            wrapper.eq("hoscode", hoscode);
        }
        //  调用方法实现分页查询
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, wrapper);

        return Result.ok(hospitalSetPage);
    }
    //  4、添加医院设置
    @ApiOperation(value = "添加医院设置")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet (@RequestBody HospitalSet hospitalSet) {
        //  设置状态， 1 能用 0 不能用
        hospitalSet.setStatus(1);
        //  签名密钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        //  调用service
        boolean save = hospitalSetService.save(hospitalSet);
        if(save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
    //  5、根据id获取医院设置
    @ApiOperation(value = "根据id获取医院设置")
    @GetMapping("getHospSet/{id}")
    public Result getHospSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    //  6、修改医院设置
    @ApiOperation(value = "修改医院设置")
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if(flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //  7、批量删除医院设置
    @ApiOperation(value = "批量删除医院设置")
    @DeleteMapping("batchRemove")
    public Result batchRemoveHospitalSet(@RequestBody List<Long> idList) {
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }
    //  8、医院设置锁定和解锁
    @ApiOperation(value = "医院设置锁定和解锁")
    @PutMapping("lockHospital/{id}/{status}")
    public Result lockHospital(@PathVariable Long id, @PathVariable Integer status){
        //  根据id查找医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //  修改状态
        hospitalSet.setStatus(status);
        //  调用方法
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }
    //  9、发送签名秘钥
    @ApiOperation(value = "发送签名秘钥")
    @PutMapping("sendKey/{id}")
    public Result sendHospitalSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        return Result.ok();
    }
}
