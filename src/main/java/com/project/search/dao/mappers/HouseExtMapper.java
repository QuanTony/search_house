package com.project.search.dao.mappers;

import com.project.search.dao.model.House;
import com.project.search.entity.param.DataTableSearch;
import com.project.search.entity.param.RentSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseExtMapper {

    List<House> getHouseList(DataTableSearch dataTableSearch);

    List<House> getIndexHouseList(RentSearch rentSearch);

    int updateHouseStatus(@Param("id") Long id,@Param("status") int status);


}
