package co.eluminum.api.email.common.exception;

import co.eluminum.api.email.common.config.mdc.MDCContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Log4j2
public class ResponseExceptionHandler {

  @Value("${exception.ref}")
  private String exceptionRef;

  @Value("${message.error.general}")
  private String generalErrorMessage;

  @Value("${message.error.hint.client}")
  private String clientErrorMessage;

  @Value("${message.error.hint.validation}")
  private String validationErrorMessage;

  @ExceptionHandler({WebExchangeBindException.class, Exception.class})
  public Mono<ResponseEntity<ErrorResponse>> handleException(Exception ex) {
    return Mono.deferContextual(
        contextView -> {
          MDCContext mdc = contextView.getOrDefault(MDCContext.class, null);
          String correlationId = (mdc != null) ? mdc.getCorrelationId() : null;
          RecoveryHints hints = resolveRecoveryHints(ex);
          String errorMessage = null;

          log.error(
              "{} exception=\"{}\" reason=\"{}",
              mdc.getLogPrefix(),
              ex.getClass().getName(),
              ex.getMessage());

          HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

          if (ex instanceof WebExchangeBindException bindEx) {
            List<String> errors =
                bindEx.getBindingResult().getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            errorMessage = String.join(", ", errors);
            status = (HttpStatus) bindEx.getStatusCode();
          }

          if ("Private".equalsIgnoreCase(exceptionRef)) {
            errorMessage = (errorMessage != null) ? errorMessage : ex.getMessage();
          } else {
            errorMessage = (errorMessage != null) ? errorMessage : clientErrorMessage;
          }

          ErrorResponse errorResponse = new ErrorResponse(correlationId, errorMessage, hints);
          return Mono.just(ResponseEntity.status(status).body(errorResponse));
        });
  }

  private RecoveryHints resolveRecoveryHints(Exception ex) {
    String classname = ex.getClass().getSimpleName();
    if (classname != null && classname.startsWith("Missing")) {
      return new RecoveryHints("CLIENT_ERROR", "true", clientErrorMessage);
    } else if (ex instanceof WebExchangeBindException) {
      return new RecoveryHints("VALIDATION_ERROR", "true", validationErrorMessage);
    }
    return new RecoveryHints("INTERNAL_ERROR", "false", generalErrorMessage);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ErrorResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String correlationId;

    private String errorMessage;
    private RecoveryHints recoveryHints;
  }

  @Data
  @AllArgsConstructor
  public static class RecoveryHints {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorType;

    private String retryable;
    private String recommendedAction;
  }
}
