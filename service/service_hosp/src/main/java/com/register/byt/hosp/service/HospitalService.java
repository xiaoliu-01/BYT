package com.register.byt.hosp.service;

import com.register.model.entity.hosp.Hospital;
import com.register.model.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
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
     * 根据查询条件，获取分页医院数据列表
     * @param page 当前页
     * @param limit 页面大小
     * @param hospitalQueryVo 查询条件
     * @return
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

    /**
     * 根据医院名称获取医院列表
     * @param hosName 医院名称
     * @return
     */
    List<Hospital> getHospitalListByHosName(String hosName);

    /**
     * 根据医院Code获取医院预约挂号详情
     * @param hosCode 医院Code
     * @return
     */
    Map<String, Object> selectHospDetailByHosCode(String hosCode);
}
