package com.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.SetmealDish;
import com.reggie.entity.Category;
import com.reggie.entity.Setmeal;
import com.reggie.service.CategoryService;
import com.reggie.service.SetmealDishService;
import com.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * add new combo
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value="setmealCache", allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("combo info: {}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("added successfully");
    }

    /**
     * combo pagination
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //create pagination
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        //create query wrapper
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //add condition
        queryWrapper.like(name != null, Setmeal::getName, name);
        //add sorting
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        //object copy
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {

            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }



    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") Integer status,@RequestParam List<Long> ids) {
        setmealService.updateSetmealStatusById(status, ids);
        return R.success("status updated successfully");
    }


    /**
     * deleting
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value="setmealCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids: {}", ids);
        setmealService.removeWithDish(ids);
        return R.success("deleted successfully");
    }

    /**
     * display combo data via id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getData(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getDate(id);
        return R.success(setmealDto);
    }

    /**
     * based on condition to get combo data
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value="setmealCache", key="#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list( Setmeal setmeal){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() !=null, Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() !=null, Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

    @PutMapping
    public R<String> edit(@RequestBody SetmealDto setmealDto){

        if(setmealDto == null){
            return R.error("request exception");
        }
        if(setmealDto.getSetmealDishes()==null){
            return R.error("there is no cuisine in the combo");
        }
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long setmealId = setmealDto.getId();

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(queryWrapper);

        //为setmeal_dish表填充相关的属性
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        //批量把setmealDish保存到setmeal_dish表
        setmealDishService.saveBatch(setmealDishes);
        setmealService.updateById(setmealDto);

        return R.success("updated successfully");
    }

}
