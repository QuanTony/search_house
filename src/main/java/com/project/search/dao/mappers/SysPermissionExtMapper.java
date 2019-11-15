package com.project.search.dao.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionExtMapper {

    Long checkPermission(@Param("userId") Long userId, @Param("codes") List<String> codes);

    List<String> getUserActionUrls(Long userId);
}
