package com.wang.yygh.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.yygh.common.result.Result;
import com.wang.yygh.enums.OrderStatusEnum;
import com.wang.yygh.model.order.OrderInfo;
import com.wang.yygh.order.service.OrderService;
import com.wang.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/admin/order/orderInfo")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "訂單列表,帶分頁查詢")
    @GetMapping("{page}/{limit}")
    public Result getList(@PathVariable Long page,
                          @PathVariable Long limit,
                          OrderQueryVo orderQueryVo) {
        Page<OrderInfo> param = new Page<>(page, limit);

        IPage<OrderInfo> orderInfoIPage = orderService.selectPage(param, orderQueryVo);

        return Result.ok(orderInfoIPage);
    }

    @ApiOperation(value = "獲取訂單狀態")
    @GetMapping("getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation(value = "訂單詳情")
    @GetMapping("show/{id}")
    public Result show(@PathVariable Long id) {
        Map<String, Object> show = orderService.show(id);
        return Result.ok(show);
    }
}
