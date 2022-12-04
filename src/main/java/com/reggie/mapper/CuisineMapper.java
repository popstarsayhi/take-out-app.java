package com.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reggie.entity.Cuisine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CuisineMapper extends BaseMapper<Cuisine> {
}
