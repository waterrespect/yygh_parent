package com.wang.yygh.user.service.Impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.yygh.common.exception.YyghException;
import com.wang.yygh.common.helper.JwtHelper;
import com.wang.yygh.common.result.ResultCodeEnum;
import com.wang.yygh.enums.AuthStatusEnum;
import com.wang.yygh.model.user.Patient;
import com.wang.yygh.model.user.UserInfo;
import com.wang.yygh.user.mapper.UserInfoMapper;
import com.wang.yygh.user.service.PatientService;
import com.wang.yygh.user.service.UserInfoService;
import com.wang.yygh.vo.user.LoginVo;
import com.wang.yygh.vo.user.UserAuthVo;
import com.wang.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PatientService patientService;

    //  1、用户手机号登录接口
    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {
        //  1、从login_vo中获取手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //  2、判断手机号和验证码是否为空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //  3、判断验证码是否一次
        //TODO  验证码
        String redisCode = redisTemplate.opsForValue().get(phone);
        if(!code.equals(redisCode)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        //  4、判断手机号是否第一次登录,查询数据库,
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.selecWXInfoOpenId(loginVo.getOpenid());
            if(null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }
        //  如果爲空，第一次登錄
        if(null == userInfo) {
            //  第一次登录，添加信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", phone);
            userInfo = baseMapper.selectOne(wrapper);
            if(null == userInfo) {
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
        }
        //  校验是否被禁用
        if(userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        //  如果不是第一次，直接登录
        //  返回登录信息，用户名
        //  返回token信息
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //TODO token
        //  生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }
    //  2、查找openid是否已經存在
    @Override
    public UserInfo selecWXInfoOpenId(String openid) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        return userInfo;
    }
    //  3、用戶認證
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //  1、根據用戶查詢信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //  2、設置用戶認證信息
        userInfo.setName(userAuthVo.getName());
        //  其他認證信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType()); //证件类型
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());     //证件编号
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());   //证件路径
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());    //认证中
        //  3、信息更新
        baseMapper.updateById(userInfo);
    }
    //TODO 平臺用戶管理

    //  1、用戶列表,帶分頁查詢
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //  1、userInfoQueryVo 獲取條件值
        String name = userInfoQueryVo.getKeyword();//   用戶名稱
        Integer status = userInfoQueryVo.getStatus();// 用戶狀態
        Integer authStatus = userInfoQueryVo.getAuthStatus();// 認證狀態
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();//    開始時間
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();//    結束時間
        //  2、對條件值進行非空判斷
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) wrapper.like("name", name);
        if(!StringUtils.isEmpty(status)) wrapper.eq("status", status);
        if(!StringUtils.isEmpty(authStatus)) wrapper.eq("auth_Status", authStatus);
        if(!StringUtils.isEmpty(createTimeBegin)) wrapper.ge("createTimeBegin", createTimeBegin);
        if(!StringUtils.isEmpty(createTimeEnd)) wrapper.le("createTimeEnd", createTimeEnd);
        //  3、調用方法
        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageParam, wrapper);
        //  封裝值
        userInfoPage.getRecords().stream().forEach(item -> {
            this.packageUserInfo(item);
        });
        return userInfoPage;
    }

    //  2、用戶鎖定
    @Override
    public void lock(Long userId, Integer status) {
        //  1、用戶查詢
        //  2、判斷
        //  3、修改
        if(status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }
    //  3、用戶詳情
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String, Object> map = new HashMap<>();
        //  1、查詢用戶信息
        //  封裝
        UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(userId));
        map.put("userInfo", userInfo);
        //  2、獲取就診人信息
        List<Patient> patientList = patientService.findAllUsrId(userId);
        map.put("patientList", patientList);
        return map;
    }

    private UserInfo packageUserInfo(UserInfo userInfo) {
        //  認證狀態，賬戶狀態
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));

        String statusString = userInfo.getStatus().intValue() == 0 ? "鎖定" : "正常";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;
    }

    //  4、認證審批
    @Override
    public void approval(Long userId, Integer authStatus) {
        //  通過，不通過
        if(2 == authStatus.intValue() || -1 == authStatus.intValue()) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }
}
