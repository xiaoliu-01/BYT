package com.register.byt.hosp.service;

import com.register.model.entity.hosp.Hospital;
import com.register.model.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-04 15:18
 */
public interface HospitalService{

    /**
     * 上传医院信息
     * @param paramMap
     */
    void saveHospital(Map<String, Object> paramMap);

    /**
     * 获取医院信息
     * @param hosCode
     */
    Hospital getHospitalByCode(String hosCode);

    /**
     * 获取医院分页列表
     * @param paramMap
     */
    Page<Hospital> getPageHospital(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    /**
     * 更新医院上线状态
     * @param paramMap
     */
    void updateStatus(String id, Integer status);

    /**
     * 获取医院详细信息
     * @param id 医院ID
     */
    Hospital getHospitalById(String id);

    /**
     * 根据医院Code获取医院名字
     * @param hosCode 医院Code
     * @return
     */
    String getHosNameByCode(String hosCode);
}
