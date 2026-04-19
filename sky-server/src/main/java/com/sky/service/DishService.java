package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

public interface DishService {
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void update(DishDTO dishDTO);

    void save(DishDTO dishDTO);

    void deleteBatch(Long[] ids);

    DishVO getById(Long id);

    /**
     * 启用或禁用菜品
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
