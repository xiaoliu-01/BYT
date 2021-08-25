package com.register.byt.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.register.byt.hosp.repository.DepartmentRepository;
import com.register.byt.hosp.service.DepartmentService;
import com.register.model.entity.hosp.Department;
import com.register.model.vo.hosp.DepartmentQueryVo;
import com.register.model.vo.hosp.DepartmentVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LLXX
 * @create 2021-08-05 10:12
 */
@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentRepository departmentRepository;

    /**
     * 上传科室信息
     * @param paramMap
     */
    @Override
    public void saveDepartment(Map<String, Object> paramMap) {
        String s = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(s, Department.class);
        String depCode = department.getDepcode();
        String hosCode = department.getHoscode();
        // 根据科室Code和医院Code进行查询
        Department targetDept = departmentRepository.getDepartmentByHoscodeAndDepcode(hosCode,depCode);
        // 判断
        if(targetDept != null){
            //dept不为null的值，则为更新
            targetDept.setUpdateTime(new Date());
            targetDept.setIsDeleted(0);
            departmentRepository.save(targetDept);
        }else {
            //dept为null的值，则为添加存操作
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> findDepartmentPage(Integer page, Integer pageSize, DepartmentQueryVo queryVo) {
        Pageable pageable = PageRequest.of(page - 1 ,pageSize);
        Department department = new Department();
        BeanUtils.copyProperties(queryVo,department);
        department.setIsDeleted(0);
        ExampleMatcher matcher = ExampleMatcher.matching();
        matcher.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Department> example = Example.of(department,matcher);
        return departmentRepository.findAll(example, pageable);
    }

    @Override
    public void removeDepartmentByCode(String hosCode,String depCode) {

        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hosCode, depCode);
        if(department != null){
            departmentRepository.deleteById(department.getId());
        }
    }

    @Override
    public List<DepartmentVo> findDeptTree(String hosCode) {
        // 创建list集合，用于最终数据封装
        List<DepartmentVo> finalVo = new ArrayList<>();
        Department department = new Department();
        department.setHoscode(hosCode);
        Example<Department> example = Example.of(department);
        // 得到医院的所有科室
        List<Department> departments = departmentRepository.findAll(example);
        Map<String, List<Department>> departmentMap = departments.stream().collect(Collectors.groupingBy(Department::getBigcode));
        departmentMap.forEach((key, deptList) -> {
            // 大科室编号
            String bigCode = key;
            DepartmentVo departmentVo = new DepartmentVo();
            // 封装大科室信息
            departmentVo.setDepcode(bigCode);
            departmentVo.setDepname(deptList.get(0).getBigname());
            // 封装子科室信息
            List<DepartmentVo> childrenVo = new ArrayList<>();
            deptList.forEach(department1 -> {
                DepartmentVo departmentVo1 = new DepartmentVo();
                departmentVo1.setDepcode(department1.getDepcode());
                departmentVo1.setDepname(department1.getDepname());
                // 封装到子科室集合
                childrenVo.add(departmentVo1);
            });
            // 把小科室list集合放到大科室children里面
            departmentVo.setChildren(childrenVo);
            // 将所有数据封装到最终的返回集合中
            finalVo.add(departmentVo);
        });
        return finalVo;
    }

    @Override
    public String getDepNameByCode(String depCode, String hosCode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hosCode, depCode);
        return department.getDepname();
    }

    @Override
    public Department getDepByHosCodeAndDepCode(String depCode , String hosCode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hosCode,depCode);
    }
}
