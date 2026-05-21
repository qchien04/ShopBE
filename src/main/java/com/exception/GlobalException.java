package com.exception;

import com.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalException {
    @ResponseBody
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse> ConversationExceptionHandler(InvalidRequestException e, WebRequest req) {
        return new ResponseEntity<ApiResponse>(new ApiResponse(e.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(UserAccountException.class)
    public ResponseEntity<ApiResponse> UserAccountExceptionHandler(UserAccountException e, WebRequest req) {

        return new ResponseEntity<ApiResponse>(new ApiResponse(e.getMessage(), false), HttpStatus.BAD_REQUEST);

    }

    @ResponseBody
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiResponse> MailExceptionHandler(MailException e, WebRequest req) {

        return new ResponseEntity<ApiResponse>(new ApiResponse(e.getMessage(), false), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e,
            WebRequest req) {

        String error = e.getBindingResult().getFieldError().getDefaultMessage();
        ErrorDetail errorDetail = new ErrorDetail("Validation error", error, LocalDateTime.now());
        return new ResponseEntity<ErrorDetail>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorDetail> NoHandlerFoundExceptionHandler(MethodArgumentNotValidException e,
            WebRequest req) {
        ErrorDetail errorDetail = new ErrorDetail("Enpoint not found", req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<ErrorDetail>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorDetail> UnauthorizedExceptionHandler(UnauthorizedException e, WebRequest req) {
        ErrorDetail errorDetail = new ErrorDetail("Unauthorized!", req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<ErrorDetail>(errorDetail, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ErrorDetail> NoPermissionExceptionHandler(NoPermissionException e, WebRequest req) {
        ErrorDetail errorDetail = new ErrorDetail("No permission", req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<ErrorDetail>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundObjectRequestException.class)
    public ResponseEntity<ErrorDetail> NotFoundObjectRequestExceptionHandler(NotFoundObjectRequestException e,
            WebRequest req) {
        ErrorDetail errorDetail = new ErrorDetail("Not found O request", req.getDescription(false),
                LocalDateTime.now());
        return new ResponseEntity<ErrorDetail>(errorDetail, HttpStatus.BAD_REQUEST);
    }
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ErrorDetail> otherExceptionHandler(Exception e,
    // WebRequest req){
    // ErrorDetail errorDetail=new
    // ErrorDetail(e.getMessage(),req.getDescription(false), LocalDateTime.now());
    // return new ResponseEntity<ErrorDetail>(errorDetail, HttpStatus.BAD_REQUEST);
    // }

}
