package co.eluminum.api.email.common.config.mdc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class MDCWebFilter implements WebFilter {

  @Value("${app.application}")
  private String application;

  @Value("${app.apiVersion}")
  private String apiVersion;

  @Autowired private Environment env;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String profile = (env.getActiveProfiles().length > 0) ? env.getActiveProfiles()[0] : "local";

    return chain
        .filter(exchange)
        .contextWrite(
            ctx ->
                ctx.put(
                    MDCContext.class,
                    MDCContext.fromExchange(exchange, application, apiVersion, profile)));
  }
}
