package com.project.search.common.exception;

import com.project.search.common.utils.ResultHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 处理异常用的切面
 * 把自己写的异常类放在这里，@ExceptionHandler(xxx.class)统一管理
 */
@ControllerAdvice(basePackages={"com.project."})
@Component
@ResponseBody
public class ExceptionHandleAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandleAdvice.class);

    @Autowired
    private ResultHelper result;

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleException(HttpServletRequest request, BusinessException exception) {
        logger.error(exception.getMessage(), exception);
        return result.newFailResult(exception.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public Object handleException(BindException exception) {
        if(null == exception){
            return result.newUnknownResult();
        }
        logger.error(exception.getMessage(), exception);

        BindingResult bindingResult = exception.getBindingResult();
        if(null == bindingResult){
            logger.warn("'bindingResult' is null.");
            return result.newUnknownResult("'bindingResult' is null.");
        }

        List<ObjectError> errorList = bindingResult.getAllErrors();
        if(null == errorList || errorList.isEmpty()){
            logger.warn("'errorList' is null or empty.");
            return result.newUnknownResult("'errorList' is null or empty.");
        }

        ObjectError error = errorList.get(0);
        if(null == error){
            logger.warn("'error' is null.");
            return result.newUnknownResult("'error' is null.");
        }
        return result.newFailResult(error.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(HttpServletRequest request, Exception exception) {
        logger.error(exception.getMessage(), exception);
        return result.newFailResult(exception.getMessage());
    }
}
