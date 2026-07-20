package co.eluminum.api.email.controller;

import co.eluminum.api.email.dto.SendEmailRequest;
import co.eluminum.api.email.dto.SendEmailResponse;
import co.eluminum.api.email.service.SendEmailService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/api/v1/email")
public class SendEmailController {

  private final SendEmailService emailService;

  public SendEmailController(SendEmailService emailService) {
    this.emailService = emailService;
  }

  @PostMapping("/send")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<SendEmailResponse> sendEmail(
      @RequestHeader(value = "X-CORRELATION-ID", required = true) String correlationId,
      @Valid @RequestBody SendEmailRequest request) {

    return emailService.sendEmail(correlationId, request);
  }
}
