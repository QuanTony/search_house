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
}
