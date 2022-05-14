package com.wang.yygh.cmn.controller;

import com.wang.yygh.cmn.service.DictService;
import com.wang.yygh.common.result.Result;
import com.wang.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {
    @Autowired
    private DictService dictService;

    //  1、根据数据id查询子数据列表
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildren/{id}")
    public Result findChildData(@PathVariable Long id) {
         List<Dict> list = dictService.findChildData(id);
         return Result.ok(list);
    }
    //  2、导出数据字典接口
    @ApiOperation(value = "导出数据字典接口")
    @GetMapping("exportData")
    public void exportDict(HttpServletResponse response) {
        dictService.exportDictData(response);
    }
    //  3、导入数据字典
    @ApiOperation(value = "导入数据字典")
    @PostMapping("importData")
    public Result importData(MultipartFile file) {
        dictService.importDictData(file);
        return Result.ok();
    }
    //  4、根据dict_code和value查询名称
    @ApiOperation(value = "根据dict_code和value查询名称")
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode,
                          @PathVariable String value) {
        String dictName = dictService.getDictName(dictCode, value);
        return dictName;
    }
    //  5、根据value查询名称
    @ApiOperation(value = "根据value查询名称")
    @GetMapping("getName/{value}")
    public String getName(@PathVariable String value) {
        String dictName = dictService.getDictName("", value);
        return dictName;
    }
    //  6、根据dict_code获取下级节点(子节点)
    @ApiOperation(value = "根据dict_code获取下级节点(子节点)")
    @GetMapping("findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }
}
