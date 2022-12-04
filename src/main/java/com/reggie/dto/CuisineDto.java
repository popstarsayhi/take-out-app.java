package com.reggie.dto;

import com.reggie.entity.Cuisine;
import com.reggie.entity.CuisineFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CuisineDto extends Cuisine {

    private List<CuisineFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
