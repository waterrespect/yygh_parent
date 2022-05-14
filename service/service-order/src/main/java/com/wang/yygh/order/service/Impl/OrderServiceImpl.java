package com.wang.yygh.order.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.common.rabbit.constant.MqConst;
import com.wang.common.rabbit.service.RabbitService;
import com.wang.yygh.common.config.helper.HttpRequestHelper;
import com.wang.yygh.common.exception.YyghException;
import com.wang.yygh.common.result.ResultCodeEnum;
import com.wang.yygh.enums.OrderStatusEnum;
import com.wang.yygh.hosp.client.HospitalFeignClient;
import com.wang.yygh.model.order.OrderInfo;
import com.wang.yygh.model.user.Patient;
import com.wang.yygh.order.mapper.OrderMapper;
import com.wang.yygh.order.service.OrderService;
import com.wang.yygh.user.client.PatientFeignClient;
import com.wang.yygh.vo.hosp.ScheduleOrderVo;
import com.wang.yygh.vo.msm.MsmVo;
import com.wang.yygh.vo.order.OrderMqVo;
import com.wang.yygh.vo.order.OrderQueryVo;
import com.wang.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    //  1、生成挂號訂單
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //  遠程調用獲取信息
        //  1、就診人信息
        Patient patient = patientFeignClient.getPatientOrder(patientId);

        //  2、獲取排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);

        //  3、時間判斷 : 未到開始時間，超過結束時間
        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
        || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }

        //  4、獲取簽名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());

        //  5、添加到訂單表
        OrderInfo orderInfo = new OrderInfo();
        //  scheduleOrderVo 數據複製到 orderInfo
        BeanUtils.copyProperties(scheduleOrderVo, orderInfo);

        String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        baseMapper.insert(orderInfo);

        //  調用醫院接口，實現醫院挂號操作
        //  hospital-manage -> controller -> HospitalController -> 预约下单AgreeAccountLendProject
        //  設置調用醫院接口需要的參數，參數放到map集合中
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);
        System.out.println("signInfoVo.Api =>" + signInfoVo.getApiUrl());
        //  請求醫院系統接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");
        System.out.println("result=>" + result);
        //todo 判斷result結果 狀態碼code
        if(200 == result.getInteger("code")) {
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");;
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");;
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);

            //  可預約數變化
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");

            //发送mq信息，更新号源和短信通知
            //  發送mq短信，號源更新
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);
            //  短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());

            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);

            orderMqVo.setMsmVo(msmVo);
            // rabbit發送消息
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

        } else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }
    //  2、根據訂單id查詢訂單詳情
    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        //  訂單狀態碼
        return this.packageOrderInfo(orderInfo);
    }

    //  3、訂單列表,帶分頁查詢
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //  1、OrderQueryVo 獲取條件值
        System.out.println("orderQueryVo =>" + orderQueryVo);
        String name = orderQueryVo.getKeyword();            //医院名称
        Long patientId = orderQueryVo.getPatientId();//就诊人id
        System.out.println("patientId=>" + patientId);
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        System.out.println("orderStatus => " + orderStatus);
        String reserveDate = orderQueryVo.getReserveDate(); //安排日期
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        //  2、對條件值進行非空判斷
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) wrapper.like("hosname", name);
        if(!StringUtils.isEmpty(patientId)) wrapper.eq("patient_id", patientId);
        if(!StringUtils.isEmpty(orderStatus)) wrapper.eq("order_status", orderStatus);
        if(!StringUtils.isEmpty(reserveDate)) wrapper.eq("reserve_date", reserveDate);
        if(!StringUtils.isEmpty(createTimeBegin)) wrapper.ge("create_time", createTimeBegin);
        if(!StringUtils.isEmpty(createTimeEnd)) wrapper.le("create_time", createTimeEnd);
        //  3、調用方法
        IPage<OrderInfo> orderInfoPage = baseMapper.selectPage(pageParam, wrapper);
        //  封裝值
        orderInfoPage.getRecords().stream().forEach(item -> {
            this.packageOrderInfo(item);
        });
        return orderInfoPage;
    }

    //  4、訂單詳情
    @Override
    public Map<String, Object> show(Long id) {
        Map<String, Object> map = new HashMap<>();
        //  1、獲取order
        OrderInfo order = this.getById(id);
        map.put("orderInfo", order);
        //  2、添加就診人信息
        Patient patient = patientFeignClient.getPatientOrder(order.getPatientId());
        map.put("patient", patient);
        return map;
    }

    //  訂單狀態碼
    private OrderInfo packageOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

}
