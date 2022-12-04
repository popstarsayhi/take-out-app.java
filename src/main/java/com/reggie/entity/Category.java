package com.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类
 */
@Data
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;


    //type 1 cuisine 2 combo
    private Integer type;


    //cate name
    private String name;


    //sort
    private Integer sort;


    //create time
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    //update time
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    //create user
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    //update user
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}
