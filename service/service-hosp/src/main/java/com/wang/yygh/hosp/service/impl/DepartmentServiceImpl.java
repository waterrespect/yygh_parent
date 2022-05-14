package com.wang.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wang.yygh.hosp.repository.DepartmentRepository;
import com.wang.yygh.hosp.service.DepartmentService;
import com.wang.yygh.model.hosp.Department;
import com.wang.yygh.vo.hosp.DepartmentQueryVo;
import com.wang.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    //  1、上传科室
    @Override
    public void save(Map<String, Object> parameterMap) {
        //  1、转换成department对象
        String parameterMapString = JSONObject.toJSONString(parameterMap);
        Department department = JSONObject.parseObject(parameterMapString, Department.class);
        //  2、判断是否存在
        Department departmentExist =departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());
        //  3、判断
        if(departmentExist != null) {
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        } else {
            department.setUpdateTime(new Date());
            department.setCreateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }
    //  2、查询科室
    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //  1、创建pageable对象,设置当前页和每页记录数
        Pageable pageable = PageRequest.of(page - 1, limit);
        //  2、创建Example对象
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //  3、转换
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);

        Example<Department> example = Example.of(department, matcher);
        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }
    //  3、删除科室
    @Override
    public void remove(String hoscode, String depcode) {
        //  1、查找
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        //  2、判断
        if(department != null) {
            //  调用方法删除
            departmentRepository.deleteById(department.getId());
        }
    }
    //  4、根据医院编号,查询医院所有科室列表
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //  1、List集合,用于最终数据封装
        List<DepartmentVo> result = new ArrayList<>();
        //  2、根据医院编号，查询医院所有科室信息
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example<Department> example = Example.of(departmentQuery);
        List<Department> departmentList = departmentRepository.findAll(example);
        //  3、根据大科室 bigcode 分组,获得每个大可是厘米按下级子科室
        //  key，String=>大科室的编号,value 小科室的信息
        Map<String, List<Department>> departmentMap =
                departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //  4、遍历map
        for(Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            //  大科室编号
            String bigCode = entry.getKey();
            //  大可是编号对应的全局数据
            List<Department> departmentList1 = entry.getValue();

            //  封装大科室
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigCode);
            departmentVo1.setDepname(departmentList1.get(0).getBigname());
            //  封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for(Department department : departmentList1) {
                DepartmentVo departmentVo2 = new DepartmentVo();
                departmentVo2.setDepcode(department.getDepcode());
                departmentVo2.setDepname(department.getDepname());
                //  封装到list中
                children.add(departmentVo2);
            }
            //  把小科室放到大科室中
            departmentVo1.setChildren(children);
            //  放到最终result中
            result.add(departmentVo1);
        }
        return result;
    }
    //  5、根据科室编号，和医院编号，查询科室名称
    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department =
                departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department != null) {
            return department.getDepname();
        }
        return null;
    }

    //  6、根据科室编号，和医院编号，查询科室信息
    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }

}
