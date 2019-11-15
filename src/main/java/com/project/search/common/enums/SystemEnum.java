package com.project.search.common.enums;

/**
 * 
 * @author Tony
 *
 */
public enum SystemEnum {

	SUCCESS("000000", "操作成功"),
    FAIL("000001", "系统繁忙请稍后再试！"),
    UNKNOW_ERROR("000002","未知错误");

    // 成员变量
    private String index;
    private String descr;  

    // 构造方法  
    SystemEnum(String index, String descr) {
        this.descr = descr;  
        this.index = index;  
    }  
    //通过索引获取描述
    public static String getDescr(String index) {  
        for (SystemEnum c : SystemEnum.values()) {  
            if (c.getIndex().equals(index)) {  
                return c.descr;  
            }  
        }  
        return null;  
    } 
    //通过描述获取索引
    public static String getIndex(String descr) { 
    	if(descr==null)descr="";
        for (SystemEnum c : SystemEnum.values()) { 
            if (descr.equals(c.descr)) {  
                return c.index;  
            }  
        }  
        return "";  
    }
    // get set 方法  
    public String getDescr() {  
        return descr;  
    }  
    public void setDescr(String descr) {  
        this.descr = descr;  
    }
    public String getIndex()
    {
        return index;
    }
    public void setIndex(String index)
    {
        this.index = index;
    }  
}
