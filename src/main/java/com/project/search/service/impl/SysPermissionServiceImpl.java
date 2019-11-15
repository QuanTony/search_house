package com.project.search.service.impl;


import com.project.search.common.utils.StringUtils;
import com.project.search.dao.mappers.SysPermissionExtMapper;
import com.project.search.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SysPermissionServiceImpl implements SysPermissionService{

    @Autowired
    private SysPermissionExtMapper sysPermissionMapper;

    @Override
    public Long checkPermission(Long userId, String codes) {
        List<String> permissionCode = StringUtils.substringString(codes);
        return sysPermissionMapper.checkPermission(userId,permissionCode);
    }

    @Override
    public List<String> getUserActionUrls(Long userId) {
        return sysPermissionMapper.getUserActionUrls(userId);
    }
}
