package com.register.byt.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.user.mapper.PatientMapper;
import com.register.byt.user.remote.CmnRemoteClient;
import com.register.byt.user.service.PatientService;
import com.register.model.entity.user.Patient;
import com.register.model.enums.DictEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-17 8:58
 */
@Service
@Slf4j
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Resource
    private CmnRemoteClient cmnRemoteClient;

    @Override
    public List<Patient> findAllUserId(Long userId) {
        // 根据 userId 查询所有就诊人信息列表
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<Patient> patients = this.list(wrapper);
        patients.stream().forEach(patient -> {
            packPatient(patient);
        });
        return patients;
    }

    @Override
    public Patient getPatientById(Long id) {
        return packPatient(this.getById(id));
    }

    // Patient对象里面其他参数封装
    private Patient packPatient(Patient patient) {
        HashMap<String, Object> map = new HashMap<>();
        //根据证件类型编码，获取证件类型具体
        //联系人证件
        String certificatesType = cmnRemoteClient.getDictName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
        //联系人证件类型
        String contactsCertificatesType = cmnRemoteClient.getDictName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getContactsCertificatesType());
        // 得到省市区
        String province = cmnRemoteClient.getDictName(patient.getProvinceCode());
        String city = cmnRemoteClient.getDictName(patient.getCityCode());
        String district = cmnRemoteClient.getDictName(patient.getDistrictCode());
        map.put("certificatesType",certificatesType);
        map.put("contactsCertificatesType",contactsCertificatesType);
        map.put("province",province);
        map.put("city",city);
        map.put("district",district);
        map.put("fullAddress",province + city + district + patient.getAddress());
        patient.setParam(map);
        return patient;
    }
}
