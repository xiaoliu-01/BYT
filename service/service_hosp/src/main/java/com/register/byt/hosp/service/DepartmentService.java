package com.register.byt.hosp.service;

import com.register.model.entity.hosp.Department;
import com.register.model.vo.hosp.DepartmentQueryVo;
import com.register.model.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-05 10:11
 */
public interface DepartmentService  {
    void saveDepartment(Map<String, Object> paramMap);

    Page<Department> findDepartmentPage(Integer page, Integer pageSize, DepartmentQueryVo queryVo);

    void removeDepartmentByCode(String hosCode,String depCode);

    /**
     *  根据医院Code,查询医院所有科室列表
     * @param hosCode 医院Code
     * @return
     */
    List<DepartmentVo> findDeptTree(String hosCode);

    /**
     * 根据科室编号，和医院编号，查询科室名称
     * @param depCode 科室编号
     * @param hosCode 医院编号
     * @return
     */
    String getDepNameByCode(String depCode, String hosCode);

    /**
     * 根据科室编号，和医院编号，查询科室信息
     * @param depCode 科室编号
     * @param hosCode 医院编号
     * @return
     */
    Department getDepByHosCodeAndDepCode(String depCode, String hosCode);
}
