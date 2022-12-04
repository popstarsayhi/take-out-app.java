package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.CustomException;
import com.reggie.entity.Cuisine;
import com.reggie.entity.Setmeal;
import com.reggie.service.CuisineService;
import com.reggie.service.SetmealService;
import com.reggie.entity.Category;
import com.reggie.mapper.CategoryMapper;
import com.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CuisineService cuisineService;

    @Autowired
    private SetmealService setmealService;

    /**
     * based on id, delete category
     * before deleting, need to check if category is related to cuisine or combo
     * @param id
     */
    @Override
    public void remove(Long id) {

        LambdaQueryWrapper<Cuisine> cuisineLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cuisineLambdaQueryWrapper.eq(Cuisine::getCategoryId,id);
        int count1 = cuisineService.count(cuisineLambdaQueryWrapper);

        //if category is related to cuisine, if it is, throw exception
        if(count1 > 0){
            throw new CustomException("this category is related to cuisine, cannot be deleted");
        }

        //if category is related to combo, if it is, throw exception
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if(count2 > 0){
            throw new CustomException("this category is related to combo, cannot be deleted");
        }

        //delete category
        super.removeById(id);
    }

}
