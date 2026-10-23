package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据动态条件统计用户数量
     *
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
