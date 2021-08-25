package com.register.byt.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.register.model.entity.order.OrderInfo;
import com.register.model.vo.order.OrderCountQueryVo;
import com.register.model.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-20 11:02
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 统计订单
     */
    List<OrderCountVo> selectOrderInfoCount(@Param("vo") OrderCountQueryVo vo);
}
