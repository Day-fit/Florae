package pl.Dayfit.Florae.ExceptionHandlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pl.Dayfit.Florae.Exceptions.AssociationException;
import pl.Dayfit.Florae.Exceptions.DeviceOfflineException;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthenticationException(AuthenticationException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(DeviceOfflineException.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> handleDeviceOfflineException(DeviceOfflineException exception)
    {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHttpMessageNotReadableException()
    {
        return Map.of("error", "Invalid request body");
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNoSuchElementException(NoSuchElementException exception)
    {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleAccessDeniedException(AccessDeniedException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(AssociationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleApiKeyAssociationException(AssociationException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOtherExceptions(Exception exception) {
        return Map.of("error", exception.getMessage());
    }
}
