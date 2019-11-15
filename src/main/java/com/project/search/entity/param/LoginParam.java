package com.project.search.entity.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;


@Data
public class LoginParam {
    @NotBlank(message = "登录名不能为空")
    @ApiModelProperty(value = "登录名", example = "name",required = true)
    private String loginName;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", example = "123",required = true)
    private String password;

    @ApiModelProperty(value = "验证码", example = "abcd",required = false)
    private String captchaCode;

    @ApiModelProperty(value = "验证码Key", example = "123",required = false)
    private String captchaCodeKey;
}
