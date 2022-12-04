package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.CustomException;
import com.reggie.dto.CuisineDto;
import com.reggie.entity.Cuisine;
import com.reggie.entity.CuisineFlavor;
import com.reggie.mapper.CuisineMapper;
import com.reggie.service.CuisineFlavorService;
import com.reggie.service.CuisineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CuisineServiceImpl extends ServiceImpl<CuisineMapper, Cuisine> implements CuisineService {

    @Autowired
    private CuisineFlavorService cuisineFlavorService;

    /**
     * add new cuisine and cuisine flavor
     * @param cuisineDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(CuisineDto cuisineDto) {
        //save cuisine info to dish table
        this.save(cuisineDto);
        //get cuisine id
        Long cuisineId = cuisineDto.getId();

        //get flavors
        List<CuisineFlavor> flavors = cuisineDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(cuisineId);
            return item;
        }).collect(Collectors.toList());

        //save flavor to dish_flavor table
        cuisineFlavorService.saveBatch(flavors);
    }

    /**
     * based on if, serach for cuisine and falvor info
     * @param id
     * @return
     */
    @Override
    public CuisineDto getByIdWithFlavor(Long id) {
        //search cuisine basic info from dish
        Cuisine cuisine = this.getById(id);

        CuisineDto cuisineDto = new CuisineDto();
        BeanUtils.copyProperties(cuisine,cuisineDto);

        //search for flavor based on dis_flavor
        LambdaQueryWrapper<CuisineFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CuisineFlavor::getDishId,cuisine.getId());
        List<CuisineFlavor> flavors = cuisineFlavorService.list(queryWrapper);
        cuisineDto.setFlavors(flavors);

        return cuisineDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(CuisineDto cuisineDto) {
        //update dish table
        this.updateById(cuisineDto);

        //clean current cuisine's flavor (delete)
        LambdaQueryWrapper<CuisineFlavor> queryWrapper = new LambdaQueryWrapper();
        cuisineFlavorService.remove(queryWrapper.eq(CuisineFlavor::getDishId, cuisineDto.getId()));


        //update dis_flavor table (insert)
        List<CuisineFlavor> flavors = cuisineDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(cuisineDto.getId());
            return item;
        }).collect(Collectors.toList());

        cuisineFlavorService.saveBatch(flavors);

    }

    /**
     * delete cuisine( sinlge or bulk)
     * @param ids
     */
    @Override
    @Transactional
    public void deletewithFlavor(List<Long> ids) {
        // select count(*) from dish where id in (ids) and state=1;
        LambdaQueryWrapper<Cuisine> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Cuisine::getId, ids);
        queryWrapper.eq(Cuisine::getStatus, 1);

        int count = cuisineFlavorService.count();

        if(count > 0){
            throw new CustomException("this cuisine is on sell, cannot delete");
        }

        //if status = 0, del
        this.removeByIds(ids);

        //delete flavor information
        LambdaQueryWrapper<CuisineFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(CuisineFlavor::getDishId, ids);

        cuisineFlavorService.remove(queryWrapper1);

    }

    /**
     * update status
     * @param status
     * @param ids
     */
    @Override
    public void updateCuisineStatusById(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Cuisine> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ids !=null,Cuisine::getId,ids);
        List<Cuisine> list = this.list(queryWrapper);

        for (Cuisine cuisine : list) {
            if (cuisine != null){
                cuisine.setStatus(status);
                this.updateById(cuisine);
            }
        }
    }

}
