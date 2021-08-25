package com.register.byt.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author LLXX
 * @create 2021-08-15 15:37
 */
public interface OssService {

    /**
     * 文件上传
     * @param file
     * @return
     */
    String upload(MultipartFile file);
}
