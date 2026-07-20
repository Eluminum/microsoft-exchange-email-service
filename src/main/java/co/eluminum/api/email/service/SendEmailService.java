package co.eluminum.api.email.service;

import co.eluminum.api.email.dto.SendEmailRequest;
import co.eluminum.api.email.dto.SendEmailResponse;
import reactor.core.publisher.Mono;

public interface SendEmailService {
  /**
   * Dispatches an email request to Microsoft Graph. Returns a Mono containing the delivery status
   * and ticket.
   */
  Mono<SendEmailResponse> sendEmail(String correlationId, SendEmailRequest request);
}
