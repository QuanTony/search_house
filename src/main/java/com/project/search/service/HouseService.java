package com.project.search.service;

import com.project.search.dao.model.SupportAddress;
import com.project.search.entity.param.DataTableSearch;
import com.project.search.entity.param.HouseForm;
import com.project.search.entity.param.RentSearch;

public interface HouseService {
    public Object getSupportAddress();

    public Object getSupportAddress(String cityName);

    public Object getSupportRegions(String cityName);

    public Object getSupportSubwayLine(String cityName);

    public Object getSubwayStation(int subwayId);

    public Object addHouseInfo(HouseForm houseForm);

    public Object getHouseLists(DataTableSearch dataTableSearch);

    public Object changeHouseStatus(Long id,int status);

    public Object getIndexHouseLists(RentSearch rentSearch);

    public Object getHouseAllInfo(Long houseId);

    public Object getCityAndRegion(String cityName,String regionName);
}
