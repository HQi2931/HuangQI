package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    DishService dishService;
    /*
    *菜品分页查询
    * */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    /*
    * 修改菜品
    * */
    @PutMapping()
    public Result<String> update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);
        dishService.update(dishDTO);
        return Result.success("修改成功");
    }

    /*
    * 新增菜品
    * */
    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.save(dishDTO);
        return Result.success("新增成功");
    }
    /*
    * 批量删除菜品
    * */
    @DeleteMapping
    public Result delete(Long[] ids){
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    /*
    * 根据id查询菜品
    * */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("查询菜品：{}", id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }
    /*
    * 修改菜品起售停售
    * */
    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        log.info("菜品状态：{}，菜品id：{}", status, id);
        dishService.startOrStop(status, id);
        return Result.success(status == 1 ? "菜品已起售" : "菜品已停售");
    }
}
