package studio.ikara.commons.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import studio.ikara.commons.configuration.service.AbstractMessageService;
import studio.ikara.commons.exception.GenericException;

import java.net.URI;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
@Priority(0)
public class ControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAdvice.class);

    @Autowired
    private AbstractMessageService resourceService;

    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ProblemDetail> handleGenericException(GenericException ex) {
        logger.debug("Generic Exception Occurred : ", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatus(ex.getStatusCode());
        problemDetail.setTitle(ex.getMessage());
        problemDetail.setDetail(ex.getCause() != null ? ex.getCause().getMessage() : null);
        problemDetail.setProperty("exceptionId", ex.getExceptionId());
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(ex.getStatusCode()).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleOtherExceptions(Throwable ex) {
        String eId = GenericException.uniqueId();
        String msg = resourceService.getMessage(AbstractMessageService.UNKNOWN_ERROR_WITH_ID, eId);

        log.error("Error : {}", eId, ex);

        final HttpStatus status = (ex instanceof GenericException rse)
                ? HttpStatus.valueOf(rse.getStatusCode().value())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(msg);
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("exceptionId", eId);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("about:blank")); // Default type as per RFC 7807

        return ResponseEntity.status(status).body(problemDetail);
    }
}
