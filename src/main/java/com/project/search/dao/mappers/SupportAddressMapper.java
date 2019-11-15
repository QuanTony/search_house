package com.project.search.dao.mappers;

import com.project.search.dao.model.SupportAddress;
import com.project.search.dao.model.SupportAddressExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SupportAddressMapper {
    long countByExample(SupportAddressExample example);

    int deleteByExample(SupportAddressExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SupportAddress record);

    int insertSelective(SupportAddress record);

    List<SupportAddress> selectByExample(SupportAddressExample example);

    SupportAddress selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SupportAddress record, @Param("example") SupportAddressExample example);

    int updateByExample(@Param("record") SupportAddress record, @Param("example") SupportAddressExample example);

    int updateByPrimaryKeySelective(SupportAddress record);

    int updateByPrimaryKey(SupportAddress record);
}