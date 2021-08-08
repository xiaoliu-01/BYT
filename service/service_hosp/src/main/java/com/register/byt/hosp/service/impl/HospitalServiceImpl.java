package com.register.byt.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.register.byt.hosp.remote.CmnRemoteClient;
import com.register.byt.hosp.repository.HospitalRepository;
import com.register.byt.hosp.service.HospitalService;
import com.register.model.entity.hosp.Hospital;
import com.register.model.enums.DictEnum;
import com.register.model.vo.hosp.HospitalQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-04 15:19
 */
@Service
@Slf4j
public class HospitalServiceImpl implements HospitalService {

    @Resource
    private HospitalRepository hospitalRepository;

    @Resource
    private CmnRemoteClient cmnRemoteClient;

    @Override
    public void saveHospital(Map<String, Object> paramMap) {
        String s = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(s, Hospital.class);
        //判断是否存在
        Hospital targetHospital = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());
        if (targetHospital != null) {
            // 存在
            hospital.setStatus(targetHospital.getStatus());
            hospital.setCreateTime(targetHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        } else {
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    /**
     * 查询医院
     *
     * @param hosCode
     * @return Hospital
     */
    @Override
    public Hospital getHospitalByCode(String hosCode) {
        return hospitalRepository.getHospitalByHoscode(hosCode);
    }

    /**
     * 分页查询
     *
     * @param page            当前页码
     * @param limit           每页记录数
     * @param hospitalQueryVo 查询条件
     * @return
     */
    @Override
    public Page<Hospital> getPageHospital(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching();
        matcher.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> hospitalPage = hospitalRepository.findAll(example, pageable);
        // 得到医院集合
        List<Hospital> hospitals = hospitalPage.getContent();
        hospitals.stream().forEach(hosp -> {
            this.packHospital(hosp);
        });
        return hospitalPage;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        log.info("id="+id);
        log.info("status="+status);
        // 数据校验，如果传入的状态值不合法则直接返回
        if (status.intValue() == 0 || status.intValue() == 1) {
            Hospital hospital = hospitalRepository.findById(id).get();
            if (hospital != null) {
                hospital.setStatus(status);
                hospital.setUpdateTime(new Date());
                hospitalRepository.save(hospital);
            }
        }
    }

    @Override
    public Hospital getHospitalById(String id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        // 封装param数据
        hospital = packHospital(hospital);
        return hospital;
    }

    @Override
    public String getHosNameByCode(String hosCode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hosCode);
        if (hospital != null) {
            return hospital.getHosname();
        }
        return "";
    }


    // 医院设置param值
    private Hospital packHospital(Hospital hospital) {
        // 医院级别
        String hosTypeName = cmnRemoteClient.getDictName(DictEnum.HOSTYPE.getDictCode(), hospital.getHostype());
        // 获取省、市、地区
        String provinceName = cmnRemoteClient.getDictName(hospital.getProvinceCode());
        String cityName = cmnRemoteClient.getDictName(hospital.getCityCode());
        String districtName = cmnRemoteClient.getDictName(hospital.getDistrictCode());
        hospital.getParam().put("hosTypeString", hosTypeName);
        hospital.getParam().put("fullAddress", provinceName + cityName + districtName + hospital.getAddress());
        return hospital;
    }
}
