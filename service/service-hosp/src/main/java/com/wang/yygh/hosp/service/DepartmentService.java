package com.wang.yygh.hosp.service;


import com.wang.yygh.model.hosp.Department;
import com.wang.yygh.vo.hosp.DepartmentQueryVo;
import com.wang.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    //  1、上传科室
    void save(Map<String, Object> parameterMap);
    //  2、查询科室
    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);
    //  3、删除科室
    void remove(String hoscode, String depcode);
    //  4、根据医院编号,查询医院所有科室列表
    List<DepartmentVo> findDeptTree(String hoscode);
    //  5、根据科室编号，和医院编号，查询科室名称
    String getDepName(String hoscode, String depcode);
    //  6、根据科室编号，和医院编号，查询科室信息
    Department getDepartment(String hoscode, String depcode);
}
