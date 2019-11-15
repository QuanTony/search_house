package com.project.search.service;

import java.util.List;

public interface SysPermissionService {

    public Long checkPermission(Long userId, String code);

    List<String> getUserActionUrls(Long userId);
}
