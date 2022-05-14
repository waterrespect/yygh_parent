package com.wang.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.yygh.model.cmn.Dict;
import com.wang.yygh.model.hosp.HospitalSet;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
    //  1、根据数据id查询子数据列表
    List<Dict> findChildData(Long id);
    //  2、导出数据字典接口
    void exportDictData(HttpServletResponse response);
    //  3、导入数据字典
    void importDictData(MultipartFile file);
    //  4、根据dict_code和value查询
    String getDictName(String dictCode, String value);
    //  6、根据dict_code获取下级节点
    List<Dict> findByDictCode(String dictCode);
}
