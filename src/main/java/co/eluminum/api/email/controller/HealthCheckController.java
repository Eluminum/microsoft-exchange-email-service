package co.eluminum.api.email.controller;

import co.eluminum.api.email.dto.HealthResponse;
import co.eluminum.api.email.service.HealthCheckService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/api/v1/email")
public class HealthCheckController {
  @Autowired HealthCheckService healthService;

  @GetMapping(value = "/health")
  public Mono<HealthResponse> getHealth(
      @RequestHeader(value = "X-CORRELATION-ID", required = true) String correlationId) {
    return healthService.getHealth(correlationId);
  }
}
