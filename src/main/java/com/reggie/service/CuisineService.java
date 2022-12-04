package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.CuisineDto;
import com.reggie.entity.Cuisine;

import java.util.List;

public interface CuisineService extends IService<Cuisine> {

    //add new cuisine and cuisine flavor, two tables: dish and dish_flavor
    public void saveWithFlavor(CuisineDto cuisineDto);

    //based on if, serach for cuisine and falvor info
    public CuisineDto getByIdWithFlavor(Long id);

    //update cuisine and flavors
    public void updateWithFlavor(CuisineDto cuisineDto);

    //delete bulk
    public void deletewithFlavor(List<Long> ids);

    /**
     * update status
     * @param status
     * @param ids
     */
    public void updateCuisineStatusById(Integer status,List<Long> ids);
}
