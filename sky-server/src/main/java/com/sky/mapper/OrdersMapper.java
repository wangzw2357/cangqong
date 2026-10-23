package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据状态统计订单数量
     *
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 根据动态条件统计营业额
     *
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     *
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 统计指定时间区间内的销量排名前10商品
     *
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

    /**
     * 修改订单
     *
     * @param orders
     */
    void update(Orders orders);
}
