package com.register.byt.oss.utlis;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author LLXX
 * @create 2021-08-15 16:08
 */
public class OssUtils {  // Oss工具类
    /**
     * 拼接文件上传路径
     * @param model模块名
     * @param file 上传文件
     * @return
     */
    public static String getFileUploadPath(String model , MultipartFile file){
        // 得到文件名
        String filename = file.getOriginalFilename();
        // 得到文件后缀
        String fileExtension = filename.substring(filename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString().substring(0, 4).replaceAll("-", "");
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String format = formatter.format(localDate);
        // 拼接
        String filePath =  model + "/" + format + "/" + uuid + fileExtension;
        return filePath;
    }
}
