package com.novara.banking.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.net.URI;
import com.novara.banking.statement.DocumentRateLimitException;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DocumentRateLimitException.class)
    ResponseEntity<ProblemDetail> rateLimit(DocumentRateLimitException exception, HttpServletRequest request) {
        var problem=ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,exception.getMessage());problem.setTitle("DOCUMENT_RATE_LIMITED");problem.setProperty("code","DOCUMENT_RATE_LIMITED");problem.setInstance(URI.create(request.getRequestURI()));return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After",Long.toString(exception.retryAfter())).body(problem);
    }
    @ExceptionHandler(DomainException.class)
    ProblemDetail domain(DomainException exception, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(exception.status(), exception.getMessage());
        problem.setType(URI.create("https://errors.novara.example/" + exception.code().toLowerCase()));
        problem.setTitle(exception.code()); problem.setProperty("code", exception.code());
        problem.setProperty("correlationId", MDC.get("correlationId")); problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ProblemDetail validation(Exception exception, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "The request contains invalid information.");
        problem.setTitle("VALIDATION_ERROR"); problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("correlationId", MDC.get("correlationId")); problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail forbidden(AccessDeniedException exception, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        problem.setTitle("ACCESS_DENIED"); problem.setProperty("code", "ACCESS_DENIED");
        problem.setProperty("correlationId", MDC.get("correlationId")); problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception exception, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "The service could not complete the request.");
        problem.setTitle("INTERNAL_ERROR"); problem.setProperty("code", "INTERNAL_ERROR");
        problem.setProperty("correlationId", MDC.get("correlationId")); problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
