package com.wang.easyExcel;

import com.alibaba.excel.EasyExcel;
import com.wang.yygh.model.acl.User;

import java.util.ArrayList;
import java.util.List;

public class write_test {
    public static void main(String[] args) {
//        构建list集合
        List<UserData> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserData data = new UserData();
            data.setId(i);
            data.setUsername("wang" + i);
            list.add(data);
        }
        //
        String filename = "E:\\pig\\ExcelTest\\01.xlsx";
//        "E:\pig\ExcelTest"

        //  调用方法实现写操作
        EasyExcel.write(filename, UserData.class).sheet("用户信息")
                .doWrite(list);
    }
}
