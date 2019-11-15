package com.project.search.common.validate;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 具体校验是否是正数字的逻辑
 * @author dq
 * @version 1.0
 */
public class IsIntValidator implements ConstraintValidator<IsInt,String> {
    private boolean required = false;

    @Override
    public void initialize(IsInt isInt) {
        required = isInt.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        //首先判断是不是必须的，必须的才校验，不是必须的只有有值才校验
        if(required) {
            return ValidateUtils.isInt(value);
        }else {
            if(StringUtils.isEmpty(value)) {
                return true;
            }else {
                return ValidateUtils.isInt(value);
            }
        }
    }
}
