package com.register.byt.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.register.model.entity.user.Patient;

import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-17 8:57
 */
public interface PatientService extends IService<Patient> {

    /**
     * 获取就诊人列表
     * @param userId 用户ID
     * @return
     */
    List<Patient> findAllUserId(Long userId);

    /**
     * 根据用ID,获取就诊人信息
     * @param id ID
     * @return
     */
    Patient getPatientById(Long id);
}
