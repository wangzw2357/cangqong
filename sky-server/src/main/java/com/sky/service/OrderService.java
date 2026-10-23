package com.sky.service;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单条件搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各状态的订单数量统计
     *
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 查询订单详情，包含订单明细
     *
     * @param id
     * @return
     */
    OrderVO getOrderWithDetail(Long id);

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 管理端取消订单
     *
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单
     *
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单
     *
     * @param id
     */
    void complete(Long id);
}
