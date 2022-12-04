package com.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.entity.Category;
import com.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * add new category of cuisine
     * @param category
     * @return
     */

    @PostMapping
    public R<String> save(@RequestBody Category category){

        log.info("category: {}", category);
        categoryService.save(category);
        return R.success("new category added successfully");
    }


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){

        log.info("page = {}, pageSize = {}, name = {}", page, pageSize);

        //pagination constructor
        Page<Category> pageInfo = new Page<>(page, pageSize);

        //filtering name (where) constructor
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //adding filter (where name like ...)
        queryWrapper.orderByAsc(Category::getSort);

        //excute
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * delete category based on id
     * @param id
     * @return
     */
    @DeleteMapping()
    public R<String> delete(Long id){
        log.info("delete id :{}", id);

        //categoryService.removeById(id);
        categoryService.remove(id);

        return R.success("deleted successfully");
    }

    /**
     * update category based on id
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("update info: {}", category);
        categoryService.updateById(category);

        return R.success("updated successfully");
    }

    /**
     * get category list based on type
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //create query wrapper
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //add condition
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //add sorting
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }


}
