package com.wang.yygh.oss.controller;

import com.wang.yygh.common.result.Result;
import com.wang.yygh.oss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/oss/file")
public class FileApiController {

    @Autowired
    private FileService fileService;

    //  1、上傳文件到阿里雲oss
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) {
        //  1、獲取上傳文件url
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
