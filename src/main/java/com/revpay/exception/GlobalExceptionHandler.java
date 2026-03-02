package com.revpay.exception;

import com.revpay.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Global exception handler for the entire application.
 *
 * @RestControllerAdvice:
 * - Applies to all controllers.
 * - Catches exceptions thrown in any controller.
 * - Returns structured API responses instead of default error pages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all RuntimeException errors.
     *
     * Function:
     * - Triggered when any RuntimeException is thrown.
     * - Creates a standardized ApiResponse object.
     * - Returns HTTP 400 (BAD_REQUEST).
     *
     * Example cases:
     * - Invalid input
     * - Unauthorized action
     * - Business validation errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntime(RuntimeException ex) {

        // Create structured API response with error message
        ApiResponse<?> response = new ApiResponse<>(false, ex.getMessage(), null);

        // Return 400 Bad Request
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all general exceptions not caught above.
     *
     * Function:
     * - Acts as a fallback error handler.
     * - Prevents application crash.
     * - Returns generic error message to client.
     * - Returns HTTP 500 (INTERNAL_SERVER_ERROR).
     *
     * Used for:
     * - Unexpected server errors
     * - Null pointer exceptions
     * - Database failures
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception ex) {

        // Create generic error response (do not expose internal error details)
        ApiResponse<?> response =
                new ApiResponse<>(false, "Something went wrong", null);

        // Return 500 Internal Server Error
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}