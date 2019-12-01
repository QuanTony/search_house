package com.project.search.service;

import com.project.search.common.utils.ResultHelper;
import com.project.search.dao.model.SupportAddress;
import com.project.search.entity.param.DataTableSearch;
import com.project.search.entity.param.HouseForm;
import com.project.search.entity.param.MapSearch;
import com.project.search.entity.param.RentSearch;

import java.util.Map;

public interface HouseService {
    /**
     * 获取所有城市列表
     * @return
     */
    public Object getSupportAddress();

    /**
     * 根据城市名称获取城市信息
     * @param cityName
     * @return
     */
    public Object getSupportAddress(String cityName);

    /**
     * 根据城市获取旗下的地区
     * @param cityName
     * @return
     */
    public Object getSupportRegions(String cityName);

    public Object getSupportSubwayLine(String cityName);

    public Object getSubwayStation(int subwayId);

    public Object addHouseInfo(HouseForm houseForm);

    public Object getHouseLists(DataTableSearch dataTableSearch);

    public Object changeHouseStatus(Long id,int status);

    public Object getIndexHouseLists(RentSearch rentSearch);

    public Object getHouseAllInfo(Long houseId);

    public Object getCityAndRegion(String cityName,String regionName);

    public Object getBaiduMapLocation(String cityEnName, String address);

    /**
     * 获取所有地图信息
     * @param mapSearch
     * @return
     */
    public Object wholeMapQuery(MapSearch mapSearch);

    /**
     * 获取地图精准信息
     * @param mapSearch
     * @return
     */
    public Object boundMapQuery(MapSearch mapSearch);


}
