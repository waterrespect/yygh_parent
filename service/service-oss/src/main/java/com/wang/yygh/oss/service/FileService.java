package com.wang.yygh.oss.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


public interface FileService {
    //  1、上傳文件並獲取上傳文件url
    String upload(MultipartFile file);
}
