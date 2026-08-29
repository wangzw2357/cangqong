package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 查询订单详情，包含订单明细
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderWithDetail(Long id) {
        Orders orders = ordersMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 接单：待接单 -> 已接单
     *
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = ordersMapper.getById(ordersConfirmDTO.getId());
        if (orders == null || !Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders update = Orders.builder()
                .id(orders.getId())
                .status(Orders.CONFIRMED)
                .build();
        ordersMapper.update(update);
    }

    /**
     * 拒单：待接单 -> 已取消，已支付的订单标记为退款
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = ordersMapper.getById(ordersRejectionDTO.getId());
        if (orders == null || !Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders update = Orders.builder()
                .id(orders.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        if (Orders.PAID.equals(orders.getPayStatus())) {
            update.setPayStatus(Orders.REFUND);
        }
        ordersMapper.update(update);
    }

    /**
     * 管理端取消订单：待接单/已接单 -> 已取消，已支付的订单标记为退款
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = ordersMapper.getById(ordersCancelDTO.getId());
        if (orders == null
                || Orders.COMPLETED.equals(orders.getStatus())
                || Orders.CANCELLED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders update = Orders.builder()
                .id(orders.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        if (Orders.PAID.equals(orders.getPayStatus())) {
            update.setPayStatus(Orders.REFUND);
        }
        ordersMapper.update(update);
    }

    /**
     * 派送订单：已接单 -> 派送中
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null || !Orders.CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders update = Orders.builder()
                .id(orders.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        ordersMapper.update(update);
    }

    /**
     * 完成订单：派送中 -> 已完成
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null || !Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders update = Orders.builder()
                .id(orders.getId())
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .checkoutTime(LocalDateTime.now())
                .build();
        ordersMapper.update(update);
    }
}
