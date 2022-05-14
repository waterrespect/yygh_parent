package com.wang.easyExcel;

import com.alibaba.excel.EasyExcel;

public class read_test {
    public static void main(String[] args) {
        //  读取文件路径
        String filename = "E:\\pig\\ExcelTest\\01.xlsx";
        //  调用方法实现读取
        EasyExcel.read(filename, UserData.class, new ExcelListener())
                .sheet()
                .doRead();
    }
}
