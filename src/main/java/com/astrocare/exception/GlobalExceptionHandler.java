package com.astrocare.exception;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.security.access.AccessDeniedException; import org.springframework.web.bind.MethodArgumentNotValidException; import java.time.*; import java.util.stream.*;
@RestControllerAdvice public class GlobalExceptionHandler {
 record ErrorResponse(LocalDateTime timestamp,int status,String error,String message,String path) {}
 @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException e, jakarta.servlet.http.HttpServletRequest r){return build(HttpStatus.NOT_FOUND,e,r);}
 @ExceptionHandler({ForbiddenOperationException.class,AccessDeniedException.class}) ResponseEntity<ErrorResponse> forbidden(Exception e,jakarta.servlet.http.HttpServletRequest r){return build(HttpStatus.FORBIDDEN,e,r);}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException e,jakarta.servlet.http.HttpServletRequest r){String m=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining(", "));return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(),400,"Validation failed",m,r.getRequestURI()),HttpStatus.BAD_REQUEST);}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> generic(Exception e,jakarta.servlet.http.HttpServletRequest r){return build(HttpStatus.INTERNAL_SERVER_ERROR,e,r);}
 private ResponseEntity<ErrorResponse> build(HttpStatus s,Exception e,jakarta.servlet.http.HttpServletRequest r){return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(),s.value(),s.getReasonPhrase(),e.getMessage(),r.getRequestURI()),s);}
}
