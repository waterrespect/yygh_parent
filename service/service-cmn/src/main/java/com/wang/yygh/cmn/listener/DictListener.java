package com.wang.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.wang.yygh.cmn.mapper.DictMapper;
import com.wang.yygh.model.cmn.Dict;
import com.wang.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

public class DictListener extends AnalysisEventListener<DictEeVo> {
    private DictMapper dictMapper;
    public  DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //  调用方法
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        dictMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
