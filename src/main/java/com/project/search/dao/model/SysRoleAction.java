package com.project.search.dao.model;

public class SysRoleAction {
    private Integer id;

    private String roleId;

    private String roleActionId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId == null ? null : roleId.trim();
    }

    public String getRoleActionId() {
        return roleActionId;
    }

    public void setRoleActionId(String roleActionId) {
        this.roleActionId = roleActionId == null ? null : roleActionId.trim();
    }
}