package co.eluminum.api.email.service.impl;

import co.eluminum.api.email.common.config.mdc.MDCContext;
import co.eluminum.api.email.dto.HealthResponse;
import co.eluminum.api.email.service.HealthCheckService;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class HealthCheckServiceImpl implements HealthCheckService {

  private final WebClient graphWebClient;
  private final String apiVersion;

  // Inject the Client ID to identify this service principal
  @Value("${azure.identity.client-id}")
  private String clientId;

  public HealthCheckServiceImpl(
      WebClient graphWebClient, @Value("${app.apiVersion}") String apiVersion) {
    this.graphWebClient = graphWebClient;
    this.apiVersion = apiVersion;
  }

  @Override
  public Mono<HealthResponse> getHealth(String correlationId) {
    return Mono.deferContextual(
        ctx -> {
          MDCContext mdc = ctx.get(MDCContext.class);
          log.info("{} START: Health Check", mdc.getLogPrefix());

          return performCheck()
              .doFinally(_ -> log.info("{} END: Health Check", mdc.getLogPrefix()));
        });
  }

  private Mono<HealthResponse> performCheck() {
    String timestamp = Instant.now().toString();

    return graphWebClient
        .get()
        // Querying the service principal by appId is the standard way to verify app-only
        // connectivity
        .uri("/servicePrincipals(appId='{id}')", clientId)
        .retrieve()
        .toBodilessEntity()
        .thenReturn(true)
        .timeout(Duration.ofSeconds(3))
        .doOnError(e -> log.error("Graph API connectivity check failed: {}", e.getMessage()))
        .onErrorReturn(false)
        .map(isConnected -> new HealthResponse("UP", apiVersion, timestamp, isConnected));
  }
}
