package com.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.ShoppingCart;
import com.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * adding item to shopping cart
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("shopping car: {}",shoppingCart);

        //set current user id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            //added to shopping cart is dish
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //added is combo
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //SQL: select * from shopping_cart where user_id = ? and dish_id =? or setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        //search if the added item is already in shopping cart, number +1
        if(cartServiceOne != null){
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number +1 );
            cartServiceOne.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //if the added item is not in shopping cart, save to table and number is 1
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * sub shopping cart item
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {

        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
            queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

            ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
            shoppingCart1.setNumber(shoppingCart1.getNumber() - 1);
            Integer number = shoppingCart1.getNumber();

            if (number > 0) {
                shoppingCartService.updateById(shoppingCart1);
            } else if (number == 0) {
                shoppingCartService.removeById(shoppingCart1.getId());
            } else if (number < 0) {
                return R.error("cart item cannot be less than 0");
            }

            return R.success(shoppingCart1);
        }

        //combo
        Long setmealId = shoppingCart.getSetmealId();
        if (setmealId != null) {
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId).eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
            ShoppingCart shoppingCart2 = shoppingCartService.getOne(queryWrapper);
            shoppingCart2.setNumber(shoppingCart2.getNumber() - 1);
            Integer number = shoppingCart2.getNumber();

            if (number > 0) {
                shoppingCartService.updateById(shoppingCart2);
            } else if (number == 0) {
                shoppingCartService.removeById(shoppingCart2.getId());
            } else if (number < 0) {
                return R.error("cart item cannot be less than 0");
            }

            return R.success(shoppingCart2);
        }

        return R.error("please check again");

    }

    /**
     * display shopping cart
     * @param shoppingCart
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list( ShoppingCart shoppingCart){

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){

        //SQl: delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("shopping cart was removed successfully");
    }






}
