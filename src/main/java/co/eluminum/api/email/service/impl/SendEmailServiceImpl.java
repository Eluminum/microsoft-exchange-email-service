package co.eluminum.api.email.service.impl;

import co.eluminum.api.email.common.config.mdc.MDCContext;
import co.eluminum.api.email.dto.SendEmailRequest;
import co.eluminum.api.email.dto.SendEmailResponse;
import co.eluminum.api.email.service.SendEmailService;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class SendEmailServiceImpl implements SendEmailService {

  private final WebClient graphWebClient;
  private final String senderUpn;

  public SendEmailServiceImpl(
      WebClient graphWebClient, @Value("${app.email.sender-upn}") String senderUpn) {
    this.graphWebClient = graphWebClient;
    this.senderUpn = senderUpn;
  }

  @Override
  public Mono<SendEmailResponse> sendEmail(String correlationId, SendEmailRequest request) {
    Map<String, Object> emailPayload =
        Map.of(
            "message",
            Map.of(
                "subject",
                request.subject(),
                "body",
                Map.of(
                    "contentType", request.messageType(),
                    "content", request.body()),
                "toRecipients",
                request.to().stream()
                    .map(email -> Map.of("emailAddress", Map.of("address", email)))
                    .toList(),
                "ccRecipients",
                (request.cc() != null
                    ? request.cc().stream()
                        .map(email -> Map.of("emailAddress", Map.of("address", email)))
                        .toList()
                    : List.of())),
            "saveToSentItems",
            "true");

    return Mono.deferContextual(
        ctx -> {
          MDCContext mdc = ctx.get(MDCContext.class);
          long startTime = System.currentTimeMillis();
          log.info("{} START: Send Email To {} [Timestamp: {}]", mdc.getLogPrefix(), request.to(), startTime);

          return graphWebClient
              .post()
              .uri("/users/{id}/sendMail", senderUpn)
              .bodyValue(emailPayload)
              .retrieve()
              .toBodilessEntity()
              .doOnSubscribe(_ -> log.info("{} GRAPH_API_CALL: Initiating request to Microsoft Graph [Elapsed: {}ms]", 
                  mdc.getLogPrefix(), System.currentTimeMillis() - startTime))
              .thenReturn(new SendEmailResponse("TICKET-" + System.currentTimeMillis(), "SUCCESS"))
              .doOnSuccess(res -> log.info("{} GRAPH_API_SUCCESS: Received response from Microsoft Graph [Elapsed: {}ms, Status: {}]", 
                  mdc.getLogPrefix(), System.currentTimeMillis() - startTime, res.status()))
              .doOnError(
                  e -> log.error("{} Failed to send email after {}ms: {}", 
                      mdc.getLogPrefix(), System.currentTimeMillis() - startTime, e.getMessage()))
              .onErrorReturn(new SendEmailResponse(null, "FAILED"))
              .doFinally(signalType -> log.info("{} END: Send Email [Signal: {}, Total Elapsed: {}ms]", 
                  mdc.getLogPrefix(), signalType, System.currentTimeMillis() - startTime));
        });
  }
}
