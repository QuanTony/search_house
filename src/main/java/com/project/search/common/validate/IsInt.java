package com.project.search.common.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义的校验注解，校验是否是正整数
 * 如果校验失败会返回message的值
 * @author dq
 * @version 1.0
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsIntValidator.class })
public @interface IsInt {
    boolean required() default false;

    String message() default "value must be int";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
