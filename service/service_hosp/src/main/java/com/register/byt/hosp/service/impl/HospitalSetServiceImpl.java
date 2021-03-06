package com.register.byt.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.hosp.mapper.HospitalSetMapper;
import com.register.byt.hosp.service.HospitalSetService;
import com.register.model.entity.hosp.HospitalSet;
import com.register.model.vo.order.SignInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医院设置表 服务实现类
 * </p>
 *
 * @author LLXX
 * @since 2021-07-28
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {

    @Override
    public String getSingKey(String hosCode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode", hosCode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        return hospitalSet.getSignKey();
    }

    @Override
    public SignInfoVo getSignInfoVo(String hosCode) {
        SignInfoVo signInfoVo = new SignInfoVo();
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<HospitalSet>().eq("hoscode", hosCode);
        HospitalSet hospitalSet = this.getOne(wrapper);
        BeanUtils.copyProperties(hospitalSet,signInfoVo);
        return signInfoVo;
    }
}
