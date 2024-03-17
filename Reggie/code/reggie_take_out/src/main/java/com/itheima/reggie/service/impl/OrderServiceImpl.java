package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取当前用户id
        Long userId = (Long)request.getSession().getAttribute("user");
        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if(shoppingCarts == null||shoppingCarts.size()==0){
            //判断购物车为空
            throw new CustomException("购物车为空,不能下单");
        }
        //根据用户id查询用户信息
        User user = userService.getById(userId);
        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook==null){
            throw new CustomException("用户地址信息有误,不能下单");
        }
        long orderNumber = IdWorker.getId();    //订单号
        //  购物车中 商品 的总金额 需要保证在多线程的情况下 也是能计算正确的，故需要使用原子类
        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setOrderId(orderNumber);
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            // 设置用户名
            orderDetail.setName(user.getName());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());
            // 总金额 = 单价 * 份数
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());

            return orderDetail;

        }).collect(Collectors.toList());

        orders.setId(orderNumber);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);//待派送
        orders.setAmount(new BigDecimal(amount.get()));//总金额，需要 遍历购物车，计算相关金额来得到
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderNumber));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入，一条数据
        this.save(orders);
        //向订单明细表插入，多条数据
        orderDetailService.saveBatch(orderDetails);
        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }
    @Override
    public Page<OrdersDto> userPage(Integer page, Integer pageSize) {
        Page<Orders> orders = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        Long userId = (Long)request.getSession().getAttribute("user");
        // 获取用户信息
        User user = userService.getById(userId);
        AddressBook address = addressBookService.getOne(
                new LambdaQueryWrapper<AddressBook>()
                        .eq(AddressBook::getUserId, userId).
                        eq(AddressBook::getIsDefault, true));
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId,userId);
        page(orders, wrapper);
        List<OrdersDto> records = orders.getRecords().stream().map(item->{
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            ordersDto.setUserName(user.getName());
            BeanUtils.copyProperties(address, ordersDto);
            List<OrderDetail> list = orderDetailService.list(
                    new LambdaQueryWrapper<OrderDetail>()
                            .eq(OrderDetail::getOrderId, item.getId()));
            ordersDto.setOrderDetails(list);
            return ordersDto;
        }).collect(Collectors.toList());

        BeanUtils.copyProperties(orders, ordersDtoPage);
        ordersDtoPage.setRecords(records);
        return ordersDtoPage;
    }
}
