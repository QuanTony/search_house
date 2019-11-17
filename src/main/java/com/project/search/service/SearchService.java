package com.project.search.service;

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
}
