package com.project.search.dao.mappers;

import com.project.search.dao.model.HouseDetail;
import com.project.search.dao.model.HouseDetailExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HouseDetailMapper {
    long countByExample(HouseDetailExample example);

    int deleteByExample(HouseDetailExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(HouseDetail record);

    int insertSelective(HouseDetail record);

    List<HouseDetail> selectByExample(HouseDetailExample example);

    HouseDetail selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") HouseDetail record, @Param("example") HouseDetailExample example);

    int updateByExample(@Param("record") HouseDetail record, @Param("example") HouseDetailExample example);

    int updateByPrimaryKeySelective(HouseDetail record);

    int updateByPrimaryKey(HouseDetail record);
}