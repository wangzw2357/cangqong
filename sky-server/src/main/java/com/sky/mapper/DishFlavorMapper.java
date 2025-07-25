package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.DishFlavor;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 新增菜品
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除对应的口味数据
     * @param dishId
     */
    @Select("delete from dish_flavor where dish_id = #{id}")
    void delete(long dishId);

    /**
     * 批量删除菜品关联的口味数据
     * @param DishIds
     */
    void deleteByDishIds(List<Long> DishIds);
}
