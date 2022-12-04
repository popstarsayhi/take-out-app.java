package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * add new combo and save releated cuisines
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * del combos and related cuisines
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * update status
     * @param status
     * @param ids
     */
    public void updateSetmealStatusById(Integer status,List<Long> ids);

    /**
     * display combo data
     * @return
     */
    SetmealDto getDate(Long id);
}
