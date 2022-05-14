package com.wang.yygh.oss.service.Impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.wang.yygh.oss.service.FileService;
import com.wang.yygh.oss.utils.ConstantOssPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@Service
public class FileServiceImpl implements FileService {
    //  1、上傳文件並獲取上傳文件url
    @Override
    public String upload(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://" + ConstantOssPropertiesUtils.ENDPOINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantOssPropertiesUtils.ACCESSKEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantOssPropertiesUtils.BUCKET;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
            //TODO 唯一uuid進行識別 + 名字
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String objectName = file.getOriginalFilename();
            objectName = uuid + objectName;
            //TODO 按照當前日期，創建文件夾,上傳到創建的文件夾裏
            String timeUrl = new DateTime().toString("yyyy/MM/dd");
            objectName = timeUrl + "/" + objectName;
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, inputStream);
            //  路徑拼接
            String url = "https://" + bucketName + "." + ConstantOssPropertiesUtils.ENDPOINT + "/" + objectName;
            return url;
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            return null;
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

    }
}
