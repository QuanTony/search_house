package com.project.search.common.utils;

import com.project.search.common.enums.SystemEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResultHelper {
    private static final String RESULT_CODE_KEY = "code";
    private static final String RESULT_MESSAGE_KEY = "message";
    private static final String RESULT_DATA_KEY = "data";

    public <T> Map newResult(String code, String message, T data){
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT_CODE_KEY, code);
        result.put(RESULT_MESSAGE_KEY, message);
        result.put(RESULT_DATA_KEY, data);
        if (null == data){
            result.put(RESULT_DATA_KEY, null);
        }

        return result;
    }



    public <T> Map newSuccessResult(){
        return newResult(SystemEnum.SUCCESS.getIndex(), SystemEnum.SUCCESS.getDescr(), null);
    }

    public <T> Map newSuccessResult(T data){
        return newResult(SystemEnum.SUCCESS.getIndex(), SystemEnum.SUCCESS.getDescr(), data);
    }

    public <T> Map newFailResult(){
        return newResult(SystemEnum.FAIL.getIndex(), SystemEnum.FAIL.getDescr(), null);
    }

    public <T> Map newFailResult(String message){
        return newResult(SystemEnum.FAIL.getIndex(), message, null);
    }

    public <T> Map newFailResult(String code, String message){
        return newResult(code, message, null);
    }

    public <T> Map newUnknownResult(){
        return newResult(SystemEnum.UNKNOW_ERROR.getIndex(), SystemEnum.UNKNOW_ERROR.getDescr(), null);
    }

    public <T> Map newUnknownResult(String message){
        return newResult(SystemEnum.UNKNOW_ERROR.getIndex(), message, null);
    }

    public <T> Map newUnknownResult(String code, String message){
        return newResult(code, message, null);
    }
}
