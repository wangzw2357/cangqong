package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {

    /**
     * 根据订单id查询订单明细
     *
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    /**
     * 查询指定时间区间内已完成订单的明细数据，用于导出报表
     *
     * @param begin
     * @param end
     * @return
     */
    @Select("select o.number as orderNumber, od.name as name, od.amount as amount, od.number as number, o.order_time as orderTime " +
            "from order_detail od, orders o " +
            "where od.order_id = o.id and o.status = 5 and o.order_time >= #{begin} and o.order_time <= #{end}")
    List<Map<String, Object>> listDetailByOrderTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
