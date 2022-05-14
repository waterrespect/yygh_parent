package com.wang.yygh.common.exception;

import com.wang.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(YyghException.class)
    @ResponseBody   // 可以让结果通过json形式输出
    public Result error(YyghException e) {
        e.printStackTrace();
        return Result.fail();
    }
}
