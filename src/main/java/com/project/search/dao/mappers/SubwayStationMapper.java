package com.project.search.dao.mappers;

import com.project.search.dao.model.SubwayStation;
import com.project.search.dao.model.SubwayStationExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SubwayStationMapper {
    long countByExample(SubwayStationExample example);

    int deleteByExample(SubwayStationExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SubwayStation record);

    int insertSelective(SubwayStation record);

    List<SubwayStation> selectByExample(SubwayStationExample example);

    SubwayStation selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SubwayStation record, @Param("example") SubwayStationExample example);

    int updateByExample(@Param("record") SubwayStation record, @Param("example") SubwayStationExample example);

    int updateByPrimaryKeySelective(SubwayStation record);

    int updateByPrimaryKey(SubwayStation record);
}