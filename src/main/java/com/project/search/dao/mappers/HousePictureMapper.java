package com.project.search.dao.mappers;

import com.project.search.dao.model.HousePicture;
import com.project.search.dao.model.HousePictureExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HousePictureMapper {
    long countByExample(HousePictureExample example);

    int deleteByExample(HousePictureExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(HousePicture record);

    int insertSelective(HousePicture record);

    List<HousePicture> selectByExample(HousePictureExample example);

    HousePicture selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") HousePicture record, @Param("example") HousePictureExample example);

    int updateByExample(@Param("record") HousePicture record, @Param("example") HousePictureExample example);

    int updateByPrimaryKeySelective(HousePicture record);

    int updateByPrimaryKey(HousePicture record);
}