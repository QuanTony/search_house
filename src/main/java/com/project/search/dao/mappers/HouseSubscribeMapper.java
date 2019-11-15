package com.project.search.dao.mappers;

import com.project.search.config.interf.PassPermission;
import com.project.search.dao.model.HouseSubscribe;
import com.project.search.dao.model.HouseSubscribeExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HouseSubscribeMapper {
    long countByExample(HouseSubscribeExample example);

    int deleteByExample(HouseSubscribeExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(HouseSubscribe record);

    int insertSelective(HouseSubscribe record);

    List<HouseSubscribe> selectByExample(HouseSubscribeExample example);

    HouseSubscribe selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") HouseSubscribe record, @Param("example") HouseSubscribeExample example);

    int updateByExample(@Param("record") HouseSubscribe record, @Param("example") HouseSubscribeExample example);

    int updateByPrimaryKeySelective(HouseSubscribe record);

    int updateByPrimaryKey(HouseSubscribe record);

    HouseSubscribe getHouseSubscribeByUserAndHouse(@Param("houseId")int houseId,@Param("userId")int userId);
}