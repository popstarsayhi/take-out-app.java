package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.BaseContext;
import com.reggie.common.CustomException;
import com.reggie.entity.*;
import com.reggie.service.*;
import com.reggie.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * submit orders for current user
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //get current user id
        Long currentId = BaseContext.getCurrentId();

        //get current users' shopping cart based on user id
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        if(list == null || list.size() == 0){
            throw new CustomException("no items in shopping cart");
        }

        //get user data
        User user = userService.getById(currentId);

        //get address data
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if(addressBook == null){
            throw new CustomException("address is not valid");
        }

        long orderId = IdWorker.getId(); //order id

        AtomicInteger amount = new AtomicInteger(0); //used for multiple threads

        //loop through shopping cart
        List<OrderDetail> orderDetails = list.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //insert data into order table, 1 record
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get())); //total amount of ordered items
        orders.setUserId(currentId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null? "": addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "": addressBook.getCityName())
                + (addressBook.getZipCode() == null? "": addressBook.getZipCode())
                + (addressBook.getDetail() == null? "":addressBook.getDetail()));

        this.save(orders);

        //insert data into order_detail table, multiple records depending on items
        orderDetailService.saveBatch(orderDetails);

        //clean shopping cart
        shoppingCartService.remove(queryWrapper);

    }
}
