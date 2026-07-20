package co.eluminum.api.email.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfig {
  private final WebClient webClient;
  private static final String API_MIME_TYPE = "application/json";

  @Value("${service.url}")
  private String API_BASE_URL;

  private static final String USER_AGENT = "WebClient";

  public ApplicationConfig() {
    this.webClient =
        WebClient.builder()
            .baseUrl(API_BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, API_MIME_TYPE)
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .build();
  }

  public WebClient getWebClient() {
    return webClient;
  }
}
