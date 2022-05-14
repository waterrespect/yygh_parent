package com.wang.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.yygh.cmn.listener.DictListener;
import com.wang.yygh.cmn.mapper.DictMapper;
import com.wang.yygh.cmn.service.DictService;
import com.wang.yygh.model.cmn.Dict;
import com.wang.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //  根据数据id查询子数据列表
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        List<Dict> dictlist = baseMapper.selectList(wrapper);
        //  向list集合每个dict对象中设置hasChildren
        for(Dict dict : dictlist) {
            Long dictId = dict.getId();
            boolean isChild = this.isChildren(dictId);
            dict.setHasChildren(isChild);
        }
        return dictlist;
    }

    //  判断id下面是否有子节点
    private boolean isChildren(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
    //  导出数据字典接口
    @Override
    public void exportDictData(HttpServletResponse response) {
        try {
            //  类型
            response.setContentType("application/vnd.ms-excel");
            //  编码
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            //  String fileName = URLEncoder.encode("数据字典", "UTF-8");
            String fileName = "dict";
            //  Content-disposition 以下载方式打开 、   下载路径
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
            //  查询数据库
            List<Dict> dictList = baseMapper.selectList(null);
            //  Dict - DictEeVo
            List<Object> dictVoList = new ArrayList<>();
            for(Dict dict : dictList) {
                DictEeVo dictEeVo = new DictEeVo();
                //  将dict中的值给dictEeVo
                BeanUtils.copyProperties(dict, dictEeVo);
                dictVoList.add(dictEeVo);
            }
        //  调用方法进行写操作
        EasyExcel.write(response.getOutputStream(), DictEeVo.class)
                        .sheet("dict")
                        .doWrite(dictVoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //  导入数据字典
    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //  4、根据dict_code和value查询 Name
    @Override
    public String getDictName(String dictCode, String value) {
        //  1、判断传入的数据
        if(StringUtils.isEmpty(dictCode)) {
            //  直接根据value查询
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value", value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        } else {
            //  根据dict_code查询dict对象，得到dict的id值
            //  1、通过dict_code 获得parent_id
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("dict_code", dictCode);
            Dict code_dict = baseMapper.selectOne(wrapper);
            Long parent_id = code_dict.getId();
            //  2、根据parent_id和vlaue查询
            Dict finalDict = baseMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("parent_id", parent_id)
                    .eq("value", value));
            return finalDict.getName();
        }
    }
    //  6、根据dict_code获取下级节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        //  1、根据dict_code获取对应id
        Dict dict = this.getDictByDictCode(dictCode);
        //  2、根据id获取子节点
        List<Dict> childData = this.findChildData(dict.getId());
        return childData;
    }

    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode);
        Dict code_dict = baseMapper.selectOne(wrapper);
        return code_dict;
    }
}
