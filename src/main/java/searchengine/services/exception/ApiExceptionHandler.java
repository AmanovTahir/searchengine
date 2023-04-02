package searchengine.services.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(value = ApiRequestException.class)
    public ResponseEntity<FaultResponse> handle(ApiRequestException exception) {
        log.error(exception.getMessage() + " - " + exception.getFaultResponse().getError());
        return new ResponseEntity<>(exception.getFaultResponse(), exception.getStatus());
    }
}
