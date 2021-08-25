package com.register.byt.order.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.commons.utils.RandomUtils;
import com.register.byt.exception.BytException;
import com.register.byt.order.mapper.OrderInfoMapper;
import com.register.byt.order.remote.HospitalRemoteClient;
import com.register.byt.order.remote.PatientRemoteClient;
import com.register.byt.order.service.OrderInfoService;
import com.register.byt.order.service.WxPaymentService;
import com.register.byt.rabbit.constant.MqConst;
import com.register.byt.rabbit.service.RabbitService;
import com.register.model.entity.order.OrderInfo;
import com.register.model.entity.user.Patient;
import com.register.model.enums.OrderStatusEnum;
import com.register.model.helper.HttpRequestHelper;
import com.register.model.vo.hosp.ScheduleOrderVo;
import com.register.model.vo.msm.MsmVo;
import com.register.model.vo.order.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 订单表(OrderInfo)表服务实现类
 *
 * @author makejava
 * @since 2021-08-20 10:59:44
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Resource
    private PatientRemoteClient patientRemoteClient;

    @Resource
    private HospitalRemoteClient hospitalRemoteClient;

    @Resource
    private RabbitService rabbitService;

    @Resource
    private WxPaymentService wxPaymentService;

    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        OrderInfo orderInfo = new OrderInfo();
        // 取到就诊人详细
        Patient patient = patientRemoteClient.getPatientById(patientId);
        if (patient == null) throw new BytException(ResultCodeEnum.DATA_ERROR);
        // 获取预约下单数据
        ScheduleOrderVo scheduleOrderVo = hospitalRemoteClient.getScheduleOrderVo(scheduleId);
        if (scheduleOrderVo == null) throw new BytException(ResultCodeEnum.DATA_ERROR);
        // 获取SignKey
        SignInfoVo signInfoVo = hospitalRemoteClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        if (null == signInfoVo) throw new BytException(ResultCodeEnum.PARAM_ERROR);
        // 没有号
        if (scheduleOrderVo.getAvailableNumber() <= 0) throw new BytException(ResultCodeEnum.NUMBER_NO);
        //当前时间不可以预约
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                && new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new BytException(ResultCodeEnum.TIME_NO);
        }

        BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);
        // 封装数据
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        paramMap.put("depcode", orderInfo.getDepcode());
        //paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("hosScheduleId", 1); // 写死
        paramMap.put("reserveDate", new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount", orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType", patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex", patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone", patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode", patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode", patient.getDistrictCode());
        paramMap.put("address", patient.getAddress());
        //联系人
        paramMap.put("contactsName", patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone", patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");
        if (result.getInteger("code") == 200) { // 成功
            JSONObject data = result.getJSONObject("data");
            // 预约记录唯一标识（医院预约记录主键）
            String hosRecordId = data.getString("hosRecordId");
            //预约序号
            Integer number = data.getInteger("number");
            ;
            //取号时间
            String fetchTime = data.getString("fetchTime");
            ;
            //取号地址
            String fetchAddress = data.getString("fetchAddress");
            ;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            //排班可预约数
            Integer reservedNumber = data.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = data.getInteger("availableNumber");
            // 发送MQ信息更新号源和短信通知
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setScheduleId(scheduleId);
            // 短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            HashMap<String, Object> map = new HashMap<>();
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            map.put("reserveDate", reserveDate);
            map.put("code", RandomUtils.getFourBitRandom());
            map.put("amount", scheduleOrderVo.getAmount());
            map.put("name", patient.getName());
            // 退款时间
            String quitTime = new DateTime(scheduleOrderVo.getQuitTime()).toString("yyyy-MM-dd");
            map.put("quitTime", quitTime);
            msmVo.setParam(map);
            orderMqVo.setMsmVo(msmVo);
            // 发送短信
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        } else { // 失败
            throw new BytException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }

    @Override
    public OrderInfo getOrderById(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        int orderStatus = orderInfo.getOrderStatus().intValue();
        if (orderStatus == 0) {
            orderInfo.getParam().put("orderStatusString", OrderStatusEnum.UNPAID.getComment());
        } else if (orderStatus == 1) {
            orderInfo.getParam().put("orderStatusString", OrderStatusEnum.PAID.getComment());
        } else if (orderStatus == 2) {
            orderInfo.getParam().put("orderStatusString", OrderStatusEnum.GET_NUMBER.getComment());
        } else {
            orderInfo.getParam().put("orderStatusString", OrderStatusEnum.CANCLE.getComment());
        }
        return orderInfo;
    }

    @Override
    public IPage<OrderInfo> selectPage(OrderQueryVo orderQueryVo, Page<OrderInfo> infoPage) {
        //orderQueryVo获取条件值
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq(!StringUtils.isEmpty(name), "hosname", name);
        wrapper.eq(!StringUtils.isEmpty(patientId), "patient_id", patientId);
        wrapper.eq(!StringUtils.isEmpty(orderStatus), "order_status", orderStatus);
        wrapper.eq(!StringUtils.isEmpty(reserveDate), "reserve_date", reserveDate);
        wrapper.ge(!StringUtils.isEmpty(createTimeBegin), "create_time", createTimeBegin);
        wrapper.le(!StringUtils.isEmpty(createTimeEnd), "create_time", createTimeEnd);
        Page<OrderInfo> orderInfoPage = baseMapper.selectPage(infoPage, wrapper);
        List<OrderInfo> infos = orderInfoPage.getRecords();
        infos.stream().forEach(orderInfo -> {
            String orderStatusString = OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus());
            orderInfo.getParam().put("orderStatusString", orderStatusString);
        });
        return orderInfoPage;
    }

    @Override
    public Map<String, Object> show(Long id) {
        OrderInfo orderInfo = baseMapper.selectById(id);
        String orderStatusString = OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus());
        orderInfo.getParam().put("orderStatusString", orderStatusString);
        Patient patient = patientRemoteClient.getPatientById(orderInfo.getPatientId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient", patient);
        map.put("orderInfo", orderInfo);
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelOrder(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        // 当前时间大于退号时间，不能取消预约
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()) {
            throw new BytException(ResultCodeEnum.CANCEL_ORDER_NO);
        }
        // 获取SignInfoVo对象
        SignInfoVo signInfoVo = hospitalRemoteClient.getSignInfoVo(orderInfo.getHoscode());
        if (signInfoVo == null) throw new BytException(ResultCodeEnum.DATA_ERROR);
        // 封装参数,并发送请求
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updateCancelStatus");
        if (result.getInteger("code") != 200) {
            throw new BytException(ResultCodeEnum.FAIL);
        }
        //是否支付 退款
        if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
            // 已支付 退款
            Boolean refund = wxPaymentService.refund(orderId);
            if (refund) {
                // 更改订单状态
                orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
                this.updateById(orderInfo);
                //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
                OrderMqVo orderMqVo = new OrderMqVo();
                orderMqVo.setScheduleId(orderInfo.getScheduleId());
                // 短信提示
                MsmVo msmVo = new MsmVo();
                msmVo.setPhone(orderInfo.getPatientPhone());
                Map<String, String> map = new HashMap<>();
                String reserveDateTime = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                        + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
                String title = orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle();
                map.put("reserveDate", reserveDateTime);
                map.put("title", title);
                map.put("name", orderInfo.getPatientName());
                orderMqVo.setMsmVo(msmVo);
                // 发送短信
                rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
                return true;
            }
        }
        return false;
    }

    @Override
    public void patientTips() {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("reserve_date", new DateTime().toString("yyyy-MM-dd"));
        wrapper.eq("order_status", OrderStatusEnum.PAID.getStatus());
        List<OrderInfo> orderInfos = baseMapper.selectList(wrapper);
        orderInfos.stream().forEach(orderInfo -> {
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                    + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            String title = orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle();
            Map map = new HashMap();
            map.put("reserveDate",reserveDate);
            map.put("title",title);
            map.put("name",orderInfo.getPatientName());
            msmVo.setParam(map);
            // 发送短信
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        });
    }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo countQueryVo) {
        List<OrderCountVo> selectOrderInfoCount = baseMapper.selectOrderInfoCount(countQueryVo);
        // 数量统计 y 轴
        List<Integer> count = selectOrderInfoCount.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        // 日期 X轴
        List<String> reserveDate = selectOrderInfoCount.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        Map<String, Object> map = new HashMap();
        map.put("count",count);
        map.put("reserveDate",reserveDate);
        return map;
    }
}