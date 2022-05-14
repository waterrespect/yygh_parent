package com.wang.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.yygh.model.order.OrderInfo;
import com.wang.yygh.vo.order.OrderQueryVo;

import java.util.Map;

public interface OrderService extends IService<OrderInfo> {
    //  1、生成挂號訂單
    Long saveOrder(String scheduleId, Long patientId);
    //  2、根據訂單id查詢訂單詳情
    OrderInfo getOrder(String orderId);
    //  3、訂單列表,帶分頁查詢
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);
    //  4、訂單詳情
    Map<String, Object> show(Long id);
}
