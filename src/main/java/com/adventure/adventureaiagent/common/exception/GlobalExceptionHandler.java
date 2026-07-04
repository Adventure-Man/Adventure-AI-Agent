package com.adventure.adventureaiagent.common.exception;

import com.adventure.adventureaiagent.common.enums.ErrorCode;
import com.adventure.adventureaiagent.common.resp.BaseResponse;
import com.adventure.adventureaiagent.common.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author adventure
 *
 * @description 全局处理异常
 **/
@RestControllerAdvice
@ResponseBody
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    // 拦截：未登录异常
//    @ExceptionHandler(SaTokenException.class)
//    public ResponseEntity<BaseResponse<?>> handlerException(SaTokenException e) {
//        ErrorCode errorCode = null;
//        String message = e.getMessage();
//        if(e instanceof NotLoginException){
//            log.info("NotLoginException:{} - msg:{}",e,message);
//            errorCode = ErrorCode.LOGIN_AUTH_ERROR;
//        }
//        if(e instanceof NotRoleException || e instanceof NotPermissionException){
//            log.info("NotPermission:{} - msg:{}",e,message);
//            errorCode = ErrorCode.NOT_PERMISSION;
//        }
//        return ResponseEntity.ok(new BaseResponse<>(errorCode, "token权限异常"));
//    }

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException：{} - msg:{}", e, e.getMessage());
        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SERVER_RESPONSE_ERROR, e.getMessage()));
    }

    // 全局异常拦截,  拦截：其它所有异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handlerException(Exception e) {
        log.info("Exception:{} - msg:{}", e, e.getMessage());
        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SERVER_ERROR, "系统异常"));
    }


    /**
     * 请求参数验证异常处理（@RequestBody + @Valid）
     *
     * @param ex 方法参数验证异常
     * @return 统一响应结果
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        String errorMessage = errors.values().stream()
                .findFirst()
                .orElse("参数验证失败");
        log.error("MethodArgumentNotValidException: {}", errorMessage);
        return ResponseEntity.ok(ResultUtils.error(ErrorCode.PARAMS_ERROR, errorMessage));
    }


    /**
     * 参数约束验证异常处理（@RequestParam、@PathVariable + @Validated）
     *
     * @param ex 约束违反异常
     * @return 统一响应结果
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("参数验证失败");
        log.error("ConstraintViolationException: {}", errorMessage);
        return ResponseEntity.ok(ResultUtils.error(ErrorCode.PARAMS_ERROR, errorMessage));
    }

    /**
     * 非法参数异常处理
     *
     * @param e 非法参数异常
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity.ok(ResultUtils.error(ErrorCode.PARAMS_ERROR, e.getMessage()));
    }

}

