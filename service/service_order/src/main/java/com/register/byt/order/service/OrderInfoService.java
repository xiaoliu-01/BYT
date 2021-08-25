package com.register.byt.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.register.model.entity.order.OrderInfo;
import com.register.model.vo.order.OrderCountQueryVo;
import com.register.model.vo.order.OrderQueryVo;

import java.util.Map;

/**
 * 订单表(OrderInfo)表服务接口
 *
 * @author makejava
 * @since 2021-08-20 10:59:35
 */
public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 生成订单
     * @param scheduleId
     * @param patientId
     */
    Long  saveOrder(String scheduleId, Long patientId);

    /**
     * 获取订单详细
     * @param orderId 订单ID
     * @return
     */
    OrderInfo getOrderById(String orderId);

    /**
     * 获取订单列表带分页
     * @param orderQueryVo
     * @param infoPage
     * @return
     */
    IPage<OrderInfo> selectPage(OrderQueryVo orderQueryVo, Page<OrderInfo> infoPage);

    /**
     * 订单详情
     * @param orderId 订单ID
     * @return
     */
    Map<String, Object> show(Long id);

    /**
     * 取消预约
     * @param orderId 订单ID
     * @return
     */
    boolean cancelOrder(Long orderId);

    /**
     * 就诊提醒
     */
    void patientTips();

    /**
     * 统计订单
     */
    Map<String,Object> getCountMap(OrderCountQueryVo countQueryVo);
}