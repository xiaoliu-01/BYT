package com.register.byt.oss.service.Impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.exception.BytException;
import com.register.byt.oss.service.OssService;
import com.register.byt.oss.utlis.OssProperties;
import com.register.byt.oss.utlis.OssUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author LLXX
 * @create 2021-08-15 15:38
 */
@Service
@Slf4j
public class OssServiceImpl implements OssService {

    @Override
    public String upload(MultipartFile file){
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = OssProperties.ENDPOINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = OssProperties.ACCESS_KEY_ID;
        String accessKeySecret = OssProperties.SECRET;
        String bucket = OssProperties.BUCKET;
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        String fileUploadPath = OssUtils.getFileUploadPath("byt", file);
        String url = "";
        try{
            // 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
            ossClient.putObject(bucket, fileUploadPath, file.getInputStream());
            // 上传后，返回url
            //https://srb-file-lx.oss-cn-shenzhen.aliyuncs.com/srb/2021/04/16/03b5d.jpg
            url = "https://" + bucket + "." + endpoint + "/" + fileUploadPath;
        }catch (Exception e){
            throw new BytException(ResultCodeEnum.FILE_UPLOAD_FAIL);
        }finally {
            // 关闭OSSClient。
            ossClient.shutdown();
        }
        return url;
    }
}
