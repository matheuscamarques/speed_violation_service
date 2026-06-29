package br.com.velsis.SpeedViolationService.exception;

import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Object bindingTarget = ex.getBindingResult().getTarget();
        String licensePlate = null;
        String equipmentId = null;
        if (bindingTarget instanceof CaptureRequestDTO dto) {
            licensePlate = dto.licensePlate();
            equipmentId = dto.equipmentId();
        }

        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList()
                .toString();

        LOG.warn("VALIDATION_ERROR licensePlate={} equipmentId={} path={} errors={}",
                licensePlate, equipmentId, request.getRequestURI(), fieldErrors);

        return response(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        LOG.warn("VALIDATION_ERROR path={} message=missing required header '{}'",
                request.getRequestURI(), ex.getHeaderName());

        return response(HttpStatus.BAD_REQUEST, "Missing required header", ex.getHeaderName(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        LOG.warn("VALIDATION_ERROR path={} message=missing required parameter '{}'",
                request.getRequestURI(), ex.getParameterName());

        return response(HttpStatus.BAD_REQUEST, "Missing required parameter", ex.getParameterName(), request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        LOG.warn("VALIDATION_ERROR path={} message={}", request.getRequestURI(), ex.getMessage());

        return response(HttpStatus.BAD_REQUEST, "Validation failed", null, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        LOG.warn("VALIDATION_ERROR path={} message=malformed request body", request.getRequestURI());

        return response(HttpStatus.BAD_REQUEST, "Malformed request body", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        LOG.error("INTERNAL_ERROR path={} type={} message={}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", null, request);
    }

    private static ResponseEntity<ErrorResponse> response(HttpStatus status, String error, String detail, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                error,
                detail,
                request.getRequestURI(),
                OffsetDateTime.now()
        );
        return new ResponseEntity<>(body, status);
    }
}
