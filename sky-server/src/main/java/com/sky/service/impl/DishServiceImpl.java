package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    DishMapper dishMapper;
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //1.设置分页参数
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //2.//下一条sql进行分页，自动加入limit关键字分页
        Page<Dish> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void update(DishDTO dishDTO) {
        //1. 将DTO转换为Entity
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
            
        //2.设置更新时间和更新人
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
            
        //3.修改菜品基本信息
        dishMapper.update(dish);
        
        //4.删除旧的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        
        //5.插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 为每个口味设置菜品ID
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void save(DishDTO dishDTO) {
        //1. 将DTO转换为Entity
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        
        //2.设置菜品状态、时间和操作人员
        dish.setStatus(StatusConstant.DISABLE);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dish.setCreateTime(LocalDateTime.now());
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setCategoryId(dishDTO.getCategoryId());
        
        //3.插入菜品数据
        dishMapper.insert(dish);
        
        //4.获取菜品ID（需要在insert后回填）
        Long dishId = dish.getId();
        
        //5.插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 为每个口味设置菜品ID
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void deleteBatch(Long[] ids) {
        dishMapper.deleteBatch(ids);
    }

    @Override
    public DishVO getById(Long id) {
        //1.根据id查询菜品数据
        DishVO dish = dishMapper.getById(id);
            
        //2.查询菜品对应的口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getDishFlavorById(id);
            
        //3.将DishVO进行封装并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
            
        return dishVO;
    }

    /**
     * 启用或禁用菜品
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        dishMapper.update(dish);
    }
}
