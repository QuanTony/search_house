package com.project.search.service;

import com.project.search.entity.dto.HouseBucketDTO;
import com.project.search.entity.param.MapSearch;
import com.project.search.entity.param.RentSearch;

import java.util.List;

public interface SearchService {
    /**
     * 索引目标房源
     * @param houseId
     */
    void index(int houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(String houseId);

    /**
     * 根据搜索条件获取房屋id的list集合
     * @param rentSearch
     * @return
     */
    List<Integer> query(RentSearch rentSearch);

    /**
     * 获取关键词补全
     * @return
     */
    List<String> suggest(String prefix);

    /**
     * 聚合搜索所在小区
     * @param cityName
     * @param regionName
     * @param district
     * @return
     */
    Long aggregateDistrictHouse(String cityName,String regionName,String district);

    /**
     * 聚合地图页面所有地区的具体在售房源数
     * @param cityEnName
     * @return
     */
    List<HouseBucketDTO> mapAggregate(String cityEnName);

    /**
     * 查找出地图所有的房源
     * @return
     */
    List<Integer> getMapHouse(String cityEnName, String orderBy, String orderDirection, int start, int size);

    /**
     * 精确范围数据查询
     * @return
     */
    List<Integer> getMapHouse(MapSearch mapSearch);
}
