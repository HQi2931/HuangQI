package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 根据id修改菜品基本信息和对应的口味信息
     * @param dish
     */
    void update(Dish dish);

    /**
     * 插入菜品数据
     * @param dish
     */
    void insert(Dish dish);

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    Page<Dish> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(Long[] ids);

    @Select("select * from dish where id = #{id}")
    DishVO getById(Long id);
}
