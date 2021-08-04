package com.register.byt.exception;

import com.register.byt.commons.result.ResultCodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author LLXX
 * @create 2021-07-29 14:50
 */
@Data
@ApiModel(value = "自定义全局异常类")
public class BytException extends RuntimeException{

    @ApiModelProperty(value = "异常状态码")
    private Integer code;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param message
     * @param code
     */
    public BytException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    /**
     * 接收枚举类型对象
     * @param resultCodeEnum
     */
    public BytException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "BytException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
