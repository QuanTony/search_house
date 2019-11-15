package com.project.search.common.utils;

import org.springframework.stereotype.Component;

@Component
public class TransferOrderUtil {
    public String getDbOrderBy(String orderBy){
        if (!StringUtils.isBlank(orderBy)){
            switch (orderBy){
                case "lastUpdateTime": orderBy = "last_update_time";break;
                case "createTime": orderBy = "create_time";break;
                case "price": orderBy = "price";break;
                case "area": orderBy = "area";break;
                case "distanceToSubway": orderBy = "distance_to_subway";break;
            }
        }
        return orderBy;
    }

    public String getEsOrderBy(String orderBy){
        if (!StringUtils.isBlank(orderBy)){
            switch (orderBy){
                case "last_update_time": orderBy = "lastUpdateTime";break;
                case "create_time": orderBy = "createTime";break;
                case "price": orderBy = "price";break;
                case "area": orderBy = "area";break;
                case "distance_to_subway": orderBy = "distanceToSubway";break;
            }
        }
        return orderBy;
    }
}
