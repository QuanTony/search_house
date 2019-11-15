package com.project.search.dao.mappers;

import com.project.search.dao.model.Action;
import com.project.search.dao.model.ActionExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ActionMapper {
    long countByExample(ActionExample example);

    int deleteByExample(ActionExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Action record);

    int insertSelective(Action record);

    List<Action> selectByExample(ActionExample example);

    Action selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Action record, @Param("example") ActionExample example);

    int updateByExample(@Param("record") Action record, @Param("example") ActionExample example);

    int updateByPrimaryKeySelective(Action record);

    int updateByPrimaryKey(Action record);
}