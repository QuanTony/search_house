package com.project.search.dao.mappers;

import com.project.search.dao.model.SysRoleAction;
import com.project.search.dao.model.SysRoleActionExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysRoleActionMapper {
    long countByExample(SysRoleActionExample example);

    int deleteByExample(SysRoleActionExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SysRoleAction record);

    int insertSelective(SysRoleAction record);

    List<SysRoleAction> selectByExample(SysRoleActionExample example);

    SysRoleAction selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SysRoleAction record, @Param("example") SysRoleActionExample example);

    int updateByExample(@Param("record") SysRoleAction record, @Param("example") SysRoleActionExample example);

    int updateByPrimaryKeySelective(SysRoleAction record);

    int updateByPrimaryKey(SysRoleAction record);
}