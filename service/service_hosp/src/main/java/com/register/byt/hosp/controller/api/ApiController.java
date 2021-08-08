package com.register.byt.hosp.controller.api;

import com.register.byt.commons.result.Result;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.exception.BytException;
import com.register.byt.hosp.service.DepartmentService;
import com.register.byt.hosp.service.HospitalService;
import com.register.byt.hosp.service.HospitalSetService;
import com.register.byt.hosp.service.ScheduleService;
import com.register.model.entity.hosp.Department;
import com.register.model.entity.hosp.Hospital;
import com.register.model.entity.hosp.Schedule;
import com.register.model.helper.HttpRequestHelper;
import com.register.model.vo.hosp.DepartmentQueryVo;
import com.register.model.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-04 15:23
 */
@Slf4j
@RestController
@Api(tags = {"医院管理API接口"})
@RequestMapping("/api/hosp")
public class ApiController {

    @Resource
    private HospitalService hospitalService;

    @Resource
    private HospitalSetService hospitalSetService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private ScheduleService scheduleService;

    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        // 获取医院Code
        String hosCode =(String) paramMap.get("hoscode");
        String singKey = hospitalSetService.getSingKey(hosCode);
        // 图片传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String) paramMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        paramMap.put("logoData",logoData);
        if(!HttpRequestHelper.isSignEquals(paramMap,singKey)){
            throw new BytException(ResultCodeEnum.SIGN_ERROR);
        }
        hospitalService.saveHospital(paramMap);
        return Result.ok().message("上传医院成功");
    }

    @ApiOperation(value = "获取医院信息")
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request ){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        // 获取医院Code
        String hosCode = (String) map.get("hoscode");
        String singKey = hospitalSetService.getSingKey(hosCode);
        // 验签
        if(!HttpRequestHelper.isSignEquals(map,singKey)){
            throw new BytException(ResultCodeEnum.SIGN_ERROR);
        }
        Hospital hospital = hospitalService.getHospitalByCode(hosCode);
        return Result.ok(hospital);
    }

    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        // 获取医院Code
        String hosCode =(String) paramMap.get("hoscode");
        String singKey = hospitalSetService.getSingKey(hosCode);
        // 验签判断
        //log.info("接口系统的的签名为(加密)："+MD5.encrypt(sign));
        //log.info("数据库查询的签名为："+singKey);
        if(!HttpRequestHelper.isSignEquals(paramMap,singKey)){
            throw new BytException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.saveDepartment(paramMap);
        return Result.ok().message("上传科室成功");
    }

    @ApiOperation(value = "查询科室信息")
    @PostMapping("department/list")
    public Result findDepartment(HttpServletRequest request ){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 获取医院Code
        String hosCode = (String) map.get("hoscode");
        String singKey = hospitalSetService.getSingKey(hosCode);
        // 获取当前页，页面大小
        Integer page = StringUtils.isEmpty(map.get("page")) ? 1 : Integer.parseInt((String) map.get("page"));
        Integer pageSize = StringUtils.isEmpty(map.get("limit")) ? 3 : Integer.parseInt((String) map.get("limit"));
        // 验签
        if(!HttpRequestHelper.isSignEquals(map,singKey)){
            throw new BytException(ResultCodeEnum.SIGN_ERROR);
        }

        DepartmentQueryVo queryVo = new DepartmentQueryVo();
        queryVo.setDepcode((String) map.get("depcode"));
        queryVo.setHoscode(hosCode);
        Page<Department> departmentPage = departmentService.findDepartmentPage(page, pageSize, queryVo);
        return Result.ok(departmentPage);
    }

    @ApiOperation(value = "删除科室信息")
    @PostMapping("department/remove")
    public Result removeDepartmentByCode(HttpServletRequest request ){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 获取医院Code
        String hosCode = (String) map.get("hoscode");
        String depCode = (String) map.get("depcode");
        String singKey = hospitalSetService.getSingKey(hosCode);

        if(StringUtils.isEmpty(hosCode)) {
            throw new BytException(ResultCodeEnum.PARAM_ERROR);
        }
        // 验签
        if(!HttpRequestHelper.isSignEquals(map,singKey)){
            throw new BytException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.removeDepartmentByCode(hosCode,depCode);
        return Result.ok();
    }

    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        // 获取医院Code
        String hosCode =(String) paramMap.get("hoscode");
        String singKey = hospitalSetService.getSingKey(hosCode);
        // 验签判断
        //log.info("接口系统的的签名为(加密)："+MD5.encrypt(sign));
        //log.info("数据库查询的签名为："+singKey);
        if(!HttpRequestHelper.isSignEquals(paramMap,singKey)){
            throw new BytException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.saveSchedule(paramMap);
        return Result.ok().message("上传排班成功");
    }

    @ApiOperation(value = "查询排班信息")
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request ){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 获取医院Code
        String hosCode = (String) map.get("hoscode");
        String singKey = hospitalSetService.getSingKey(hosCode);
        // 获取当前页，页面大小
        Integer page = StringUtils.isEmpty(map.get("page")) ? 1 : Integer.parseInt((String) map.get("page"));
        Integer pageSize = StringUtils.isEmpty(map.get("limit")) ? 3 : Integer.parseInt((String) map.get("limit"));
        // 参数合理化校验
        if(StringUtils.isEmpty(hosCode)) {
            throw new BytException(ResultCodeEnum.PARAM_ERROR);
        }
        // 验签
        if(!HttpRequestHelper.isSignEquals(map,singKey)){
            throw new BytException(ResultCodeEnum.SIGN_ERROR);
        }
        ScheduleQueryVo queryVo = new ScheduleQueryVo();
        queryVo.setDepcode((String) map.get("depcode"));
        queryVo.setHoscode(hosCode);
        Page<Schedule> schedulePage = scheduleService.findSchedulePage(page, pageSize, queryVo);
        return Result.ok(schedulePage);
    }

    @ApiOperation(value = "删除排班信息")
    @PostMapping("schedule/remove")
    public Result removeScheduleByCode(HttpServletRequest request ){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 获取医院Code,排班ID
        String hosCode = (String) map.get("hoscode");
        String hosScheduleId = (String) map.get("hosScheduleId");
        String singKey = hospitalSetService.getSingKey(hosCode);

        if(StringUtils.isEmpty(hosCode)) {
            throw new BytException(ResultCodeEnum.PARAM_ERROR);
        }
        // 验签
        if(!HttpRequestHelper.isSignEquals(map,singKey)){
            throw new BytException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.removeScheduleByCode(hosCode,hosScheduleId);
        return Result.ok();
    }
}

