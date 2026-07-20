package co.eluminum.api.email.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import co.eluminum.api.email.dto.SendEmailRequest;
import co.eluminum.api.email.dto.SendEmailResponse;
import co.eluminum.api.email.service.SendEmailService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class SendEmailServiceImpl implements SendEmailService {

    private final WebClient graphWebClient;
    private final String senderUpn;

    public SendEmailServiceImpl(WebClient graphWebClient, @Value("${app.email.sender-upn}") String senderUpn) {
        this.graphWebClient = graphWebClient;
        this.senderUpn = senderUpn;
    }

    @Override
    public Mono<SendEmailResponse> sendEmail(String correlationId, SendEmailRequest request) {
        Map<String, Object> emailPayload = Map.of(
        		"message", Map.of(
        				"subject", request.subject(),
        		        "body", Map.of(
        		            "contentType", request.messageType(),
        		            "content", request.body()
        		        ),
        		        "toRecipients", request.to().stream()
        		            .map(email -> Map.of("emailAddress", Map.of("address", email)))
        		            .toList(),
        		        "ccRecipients", (request.cc() != null ? request.cc().stream()
        		            .map(email -> Map.of("emailAddress", Map.of("address", email)))
        		            .toList() : List.of())
        		    ),
        		    "saveToSentItems", "true"
        );

        return graphWebClient.post()
            .uri("/users/{id}/sendMail", senderUpn)
            .bodyValue(emailPayload)
            .retrieve()
            .toBodilessEntity()
            .thenReturn(new SendEmailResponse("TICKET-" + System.currentTimeMillis(), "SUCCESS"))
            .doOnError(e -> log.error("Failed to send email: {}", e.getMessage()))
            .onErrorReturn(new SendEmailResponse(null, "FAILED"));
    }
}
