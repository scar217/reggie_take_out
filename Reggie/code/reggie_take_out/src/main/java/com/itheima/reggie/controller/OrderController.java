package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;

import com.itheima.reggie.dto.OrdersDto;

import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest request;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用户订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> Page(int page, int pageSize){
        Page<OrdersDto> ordersDtoPage = orderService.userPage(page,pageSize);
        return R.success(ordersDtoPage);
    }

    /**
     * 后台查看所有订单列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> orderList(int page,int pageSize){
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        orderService.page(pageInfo);
        return R.success(pageInfo);
    }

}
