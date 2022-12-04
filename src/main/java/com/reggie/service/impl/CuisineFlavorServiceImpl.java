package com.reggie.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.entity.CuisineFlavor;
import com.reggie.mapper.CuisineFlavorMapper;
import com.reggie.service.CuisineFlavorService;
import org.springframework.stereotype.Service;

@Service
public class CuisineFlavorServiceImpl extends ServiceImpl<CuisineFlavorMapper, CuisineFlavor> implements CuisineFlavorService {
}

