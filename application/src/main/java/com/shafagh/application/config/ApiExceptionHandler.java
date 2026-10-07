package com.shafagh.application.config;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class)
 public ProblemDetail invalid(IllegalArgumentException ex) {return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,ex.getMessage());}
}
