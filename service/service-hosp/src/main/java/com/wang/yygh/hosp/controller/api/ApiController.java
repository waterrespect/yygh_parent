package com.wang.yygh.hosp.controller.api;


import com.wang.yygh.common.config.helper.HttpRequestHelper;
import com.wang.yygh.common.config.utils.MD5;
import com.wang.yygh.common.exception.YyghException;
import com.wang.yygh.common.result.Result;
import com.wang.yygh.common.result.ResultCodeEnum;
import com.wang.yygh.hosp.service.DepartmentService;
import com.wang.yygh.hosp.service.HospitalService;
import com.wang.yygh.hosp.service.HospitalSetService;
import com.wang.yygh.hosp.service.ScheduleService;
import com.wang.yygh.model.hosp.Department;
import com.wang.yygh.model.hosp.Hospital;
import com.wang.yygh.model.hosp.Schedule;
import com.wang.yygh.vo.hosp.DepartmentQueryVo;
import com.wang.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
@SuppressWarnings({"all"})
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalsetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

//============================================Hospital==========================================================================
    //  1、上传医院接口
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request) {
        //  1、获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> parameterMap = HttpRequestHelper.switchMap(requestMap);

        //  2、传输过程中 "+" 转换为了 ' "
        String logoData = (String) parameterMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        parameterMap.put("logoData", logoData);
        System.out.println(logoData);
        //  3、获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) parameterMap.get("sign");

        //  4、根据传递过来的医院编码，查询数据库，查询签名
        String hoscode = (String) parameterMap.get("hoscode");
        String signKey = hospitalsetService.getSignKey(hoscode);

        //  5、查询出的签名进行加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //  6、判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        //  7、调用service方法
        hospitalService.save(parameterMap);
        return Result.ok();
    }

    //  2、查询医院接口
    @PostMapping("/hospital/show")
    public Result getHospital(HttpServletRequest request) {
        //  1、获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> parameterMap = HttpRequestHelper.switchMap(requestMap);
        //  2、获取医院编号
        String hoscode = (String) parameterMap.get("hoscode");

        //  3、获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) parameterMap.get("sign");

        //  4、根据传递过来的医院编码，查询数据库，查询签名
        String signKey = hospitalsetService.getSignKey(hoscode);

        //  5、查询出的签名进行加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //  6、判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 7、调用service方法实现医院编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }
//============================================Hospital==========================================================================
//============================================Department==========================================================================
    //  3、上传科室接口
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        //  1、获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> parameterMap = HttpRequestHelper.switchMap(requestMap);
        //  2、验证数字签名
        String hoscode = (String) parameterMap.get("hoscode");

        //  3、获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) parameterMap.get("sign");

        //  4、根据传递过来的医院编码，查询数据库，查询签名
        String signKey = hospitalsetService.getSignKey(hoscode);

        //  5、查询出的签名进行加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //  6、判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        //7、调用service的方法
        departmentService.save(parameterMap);
        return Result.ok();
    }
    //  4、查询科室
    @PostMapping("department/list")
    public Result findDepartment(HttpServletRequest request) {
        //  1、获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> parameterMap = HttpRequestHelper.switchMap(requestMap);
        //  2、医院编号
        String hoscode = (String) parameterMap.get("hoscode");
        //  3、当前页
        int page = StringUtils.isEmpty(parameterMap.get("page")) ? 1 : Integer.parseInt ((String) parameterMap.get("page"));
//        Integer page = (Integer) parameterMap.get("page");
        //  4、大小
        int limit = StringUtils.isEmpty(parameterMap.get("limit")) ? 1 : Integer.parseInt((String) parameterMap.get("limit"));
        //  5、验证
        check(parameterMap);
        //  6、分页查询
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        //  service
        Page<Department> pageModel =  departmentService.findPageDepartment(page, limit, departmentQueryVo);

        return Result.ok(pageModel);
    }
    //  5、删除科室
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        //  1、获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> parameterMap = HttpRequestHelper.switchMap(requestMap);
        //  2、医院编号和科室编号
        String hoscode = (String) parameterMap.get("hoscode");
        String depcode = (String) parameterMap.get("depcode");
        //  3、校验
        check(parameterMap);
        //  4、调用service
        departmentService.remove(hoscode, depcode);

        return Result.ok();
    }

//============================================Department==========================================================================
//============================================Schedule==========================================================================
    //  6、上传排版
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        //  1、获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> parameterMap = HttpRequestHelper.switchMap(requestMap);
        //  2、校验
        check(parameterMap);
        //  3、
        scheduleService.save(parameterMap);
        return  Result.ok();
    }
    //  7、查询排班
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request) {
        //  1、获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> parameterMap = HttpRequestHelper.switchMap(requestMap);
        //  2、医院编号
        String hoscode = (String) parameterMap.get("hoscode");
        String depcode = (String) parameterMap.get("depcode");
        //  3、当前页
        int page = StringUtils.isEmpty(parameterMap.get("page")) ? 1 : Integer.parseInt ((String) parameterMap.get("page"));
//        Integer page = (Integer) parameterMap.get("page");
        //  4、大小
        int limit = StringUtils.isEmpty(parameterMap.get("limit")) ? 1 : Integer.parseInt((String) parameterMap.get("limit"));
        //  5、验证
        check(parameterMap);
        //  6、分页查询
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        //  service
        Page<Schedule> pageModel = scheduleService.findPageSchedule(page, limit, scheduleQueryVo);

        return Result.ok(pageModel);
    }
    //  8、删除排班
    @PostMapping("schedule/remove")
    public Result remove(HttpServletRequest request) {
        //  1、获取传递过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> parameterMap = HttpRequestHelper.switchMap(requestMap);
        //  2、医院编号、排班编号
        String hoscode = (String) parameterMap.get("hoscode");
        String hosScheduleId = (String) parameterMap.get("hosScheduleId");
        //  3、校验
        check(parameterMap);
        //  4、service 删除
        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }
// ============================================Schedule==========================================================================

    // ============================================check==========================================================================
    public void check(Map<String, Object> parameterMap) {
        String hoscode = (String) parameterMap.get("hoscode");

        //  3、获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) parameterMap.get("sign");

        //  4、根据传递过来的医院编码，查询数据库，查询签名
        String signKey = hospitalsetService.getSignKey(hoscode);

        //  5、查询出的签名进行加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //  6、判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }
// ============================================check==========================================================================

}
