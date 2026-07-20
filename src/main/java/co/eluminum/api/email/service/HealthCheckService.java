package co.eluminum.api.email.service;

import co.eluminum.api.email.dto.HealthResponse;
import reactor.core.publisher.Mono;

public interface HealthCheckService {
  Mono<HealthResponse> getHealth(String correlationId);
}
