package com.project.search.common.exception;

/**
 * 异常处理
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    /**
     * 抓取异常的时候code均为-1，message为自定义
     * @param message
     */
    public BusinessException(String message){
        super(message);
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
