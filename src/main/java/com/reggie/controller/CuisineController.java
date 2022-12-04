package com.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.CuisineDto;
import com.reggie.entity.CuisineFlavor;
import com.reggie.entity.Category;
import com.reggie.entity.Cuisine;
import com.reggie.service.CategoryService;
import com.reggie.service.CuisineFlavorService;
import com.reggie.service.CuisineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * cuisine mangement
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class CuisineController {

    @Autowired
    private CuisineService cuisineService;

    @Autowired
    private CuisineFlavorService cuisineFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * add new cuisine
     * @param cuisineDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody CuisineDto cuisineDto){
        log.info("new added cuisine: {} ", cuisineDto);

        cuisineService.saveWithFlavor(cuisineDto);

        return R.success("added successfully");
    }

    /**
     * cuisine data pagination
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        //create a pagination
        Page<Cuisine> pageInfo = new Page<>(page, pageSize);

        //create dto b/c page needs category name
        Page<CuisineDto> cuisineDtoPage = new Page<>();

        //create a query wrapper
        LambdaQueryWrapper<Cuisine> queryWrapper = new LambdaQueryWrapper<>();

        //add where condition
        queryWrapper.like(name!=null, Cuisine::getName,name);

        //add sorting
        queryWrapper.orderByDesc(Cuisine::getName);

        //excute
        cuisineService.page(pageInfo,queryWrapper);

        //copy object
        BeanUtils.copyProperties(pageInfo,cuisineDtoPage,"records");

        List<Cuisine> records = pageInfo.getRecords();

        List<CuisineDto> list= records.stream().map((item)->{
            //create new dto object and copy item to it
            CuisineDto cuisineDto = new CuisineDto();
            BeanUtils.copyProperties(item, cuisineDto);

            //get category id
            Long categoryId = item.getCategoryId();
            //search category based on id
            Category category = categoryService.getById(categoryId);

            if(category != null){
                //get category name
                String categoryName = category.getName();
                //set dto category name
                cuisineDto.setCategoryName(categoryName);
            }

            return cuisineDto;
        }).collect(Collectors.toList());

        cuisineDtoPage.setRecords(list);

        return R.success(cuisineDtoPage);
    }

    /**
     * based on id, search for cuisne and flavor
     * @param id
     * @return
     */

    @GetMapping("/{id}")
    public R<CuisineDto> get(@PathVariable Long id){

        CuisineDto cuisineDto = cuisineService.getByIdWithFlavor(id);
        return R.success(cuisineDto);
    }

    /**
     * update new cuisine
     * @param cuisineDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody CuisineDto cuisineDto){
        log.info("new added cuisine: {} ", cuisineDto);

        cuisineService.updateWithFlavor(cuisineDto);

        //clean all cuisine
        //Set keys = redisTemplate.keys("cuisine_*");
        //redisTemplate.delete(keys);

        //clean selected category
        String key = "cuisine_" + cuisineDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("updated successfully");
    }

//    /**
//     * serach cuisine based on id
//     * @param cuisine
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Cuisine>> list(Cuisine cuisine){
//
//        LambdaQueryWrapper<Cuisine> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(cuisine.getCategoryId()!=null, Cuisine::getCategoryId,cuisine.getCategoryId());
//        queryWrapper.eq(Cuisine::getStatus,1); // 1 means on sell
//        queryWrapper.orderByAsc(Cuisine::getSort).orderByDesc(Cuisine::getUpdateTime);
//
//        List<Cuisine> list = cuisineService.list(queryWrapper);
//
//        return R.success(list);
//    }

    /**
     * change status of cuisine, bulk
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") Integer status,@RequestParam List<Long> ids) {
        cuisineService.updateCuisineStatusById(status, ids);
        return R.success("status updated successfully");
    }

    /**
     * singel del and bulk del
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        log.info("del id :{}", ids);

        cuisineService.deletewithFlavor(ids);

        return R.success("deleted successfully");
    }

    /**
     * serach cuisine based on id
     * @param cuisine
     * @return
     */
    @GetMapping("/list")
    public R<List<CuisineDto>> list(Cuisine cuisine){

        List<CuisineDto> cuisineDtoList = null;

        String key = "cuisine_" +  cuisine.getCategoryId() + "_" + cuisine.getStatus(); //dynamically create a key

        //get data from redis
        cuisineDtoList = (List<CuisineDto>) redisTemplate.opsForValue().get(key);

        //if there is, return
        if(cuisineDtoList != null){
            return R.success(cuisineDtoList);
        }

        //if there is not, need to select from mysql

        LambdaQueryWrapper<Cuisine> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(cuisine.getCategoryId()!=null, Cuisine::getCategoryId,cuisine.getCategoryId());
        queryWrapper.eq(Cuisine::getStatus,1); // 1 means on sell
        queryWrapper.orderByAsc(Cuisine::getSort).orderByDesc(Cuisine::getUpdateTime);

        List<Cuisine> list = cuisineService.list(queryWrapper);

        cuisineDtoList = list.stream().map((item) ->{

            CuisineDto cuisineDto = new CuisineDto();
            BeanUtils.copyProperties(item,cuisineDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if(category !=null){
                String categoryName = category.getName();
                cuisineDto.setCategoryName(categoryName);
            }

            Long cuisineId = item.getId();
            LambdaQueryWrapper<CuisineFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(CuisineFlavor::getDishId,cuisineId);
            List<CuisineFlavor> cuisineFlavorList = cuisineFlavorService.list(queryWrapper1);
            cuisineDto.setFlavors(cuisineFlavorList);

            return cuisineDto;
        }).collect(Collectors.toList());

        //save to redis
        redisTemplate.opsForValue().set(key,cuisineDtoList,60, TimeUnit.MINUTES);

        return R.success(cuisineDtoList);
    }

}
