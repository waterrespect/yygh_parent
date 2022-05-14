package com.wang.yygh.order.api;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.yygh.common.result.Result;
import com.wang.yygh.common.utils.AuthContextHolder;
import com.wang.yygh.enums.OrderStatusEnum;
import com.wang.yygh.model.order.OrderInfo;
import com.wang.yygh.model.user.UserInfo;
import com.wang.yygh.order.service.OrderService;
import com.wang.yygh.vo.order.OrderQueryVo;
import com.wang.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "生成挂號訂單")
    @PostMapping("/auth/submitOrder/{scheduleId}/{patientId}")
    public Result saveOrder(@PathVariable String scheduleId,
                            @PathVariable Long patientId) {
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

    @ApiOperation(value = "根據訂單id查詢訂單詳情")
    @GetMapping("auth/getOrders/{orderId}")
    public Result getOrders(@PathVariable String orderId) {
        OrderInfo orderInfo = orderService.getOrder(orderId);
        return Result.ok(orderInfo);
    }

    @ApiOperation(value = "訂單列表,帶分頁查詢")
    @GetMapping("auth/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       OrderQueryVo orderQueryVo,
                       HttpServletRequest request) {
        //  設置用戶id值
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel
                = orderService.selectPage(pageParam, orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "訂單狀態")
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

}
