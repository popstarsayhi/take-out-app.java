package com.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.entity.Orders;
import com.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * submit order
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
       log.info("order: {}", orders);
        orderService.submit(orders);
        return R.success("ordered successfully");
    }

    /**
     * list order details in backend order page
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(number!=null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);

        orderService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> orderStatusChange(@RequestBody Map<String,String> map){

        String id = map.get("id");
        Long orderId = Long.parseLong(id);
        Integer status = Integer.parseInt(map.get("status"));

        if(orderId == null || status==null){
            return R.error("invalid info");
        }
        Orders orders = orderService.getById(orderId);
        orders.setStatus(status);
        orderService.updateById(orders);

        return R.success("updated successfully");
    }

    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize){

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }



}
