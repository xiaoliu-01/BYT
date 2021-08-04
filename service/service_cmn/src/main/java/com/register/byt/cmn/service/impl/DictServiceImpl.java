package com.register.byt.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.cmn.listener.DictListener;
import com.register.byt.cmn.mapper.DictMapper;
import com.register.byt.cmn.service.DictService;
import com.register.model.entity.cmn.Dict;
import com.register.model.vo.cmn.DictEeVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author LLXX
 * @create 2021-08-01 17:08
 */
@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Resource
    private  DictMapper dictMapper;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public List<Dict> findChildrenData(Long id) {
        // 从Redis中查询是否存在缓存
        List<Dict> dictList= null;
        try {
            dictList = (List<Dict>)redisTemplate.opsForValue().get("byt:cmn:dictList:" + id);
            if(dictList != null){
                // 有缓存，则取出
                log.info("从redis中取值");
                return dictList;
            }
        } catch (Exception e) {
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));//此处不抛出异常，继续执行后面的代码

        }
        // 没有，则进行数据库查询
        log.info("开始从数据库中取值");
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        dictList = baseMapper.selectList(wrapper);
        dictList.stream().forEach(dict -> {
            Long dictId = dict.getId();
            dict.setHasChildren(isChildren(dictId));
        });
        // 存入到redis缓存中
        try {
            redisTemplate.opsForValue().set("byt:cmn:dictList:" + id,dictList,30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));//此处不抛出异常，继续执行后面的代码
        }
        return dictList;
    }

    @Override
    public void exportData(HttpServletResponse response){
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            List<Dict> dictList = dictMapper.selectList(null);
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());
            dictList.forEach(dict -> {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictEeVo);
                dictVoList.add(dictEeVo);
            });
            // 写数据
            EasyExcel.write(response.getOutputStream(),DictEeVo.class)
                    .sheet("数据字典")
                    .doWrite(dictVoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void importData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class, new DictListener(dictMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //判断id下面是否有子节点
    private boolean isChildren(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
}