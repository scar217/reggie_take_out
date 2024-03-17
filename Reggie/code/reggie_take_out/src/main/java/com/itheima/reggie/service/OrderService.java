package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    public void submit(Orders orders);
    public Page<OrdersDto> userPage(Integer page, Integer pageSize);
}
