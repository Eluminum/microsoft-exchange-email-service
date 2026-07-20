package co.eluminum.api.email.common.config.mdc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.server.ServerWebExchange;

@Getter
@Setter
@AllArgsConstructor
public class MDCContext {
  private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T' HH:mm:ss.SSSX";
  private static final Pattern IP_ADDRESS_PATTERN =
      Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

  private final String timestamp;
  private final String remoteAddress;
  private final String application;
  private final String logType;
  private final String env;
  private final String apiVersion;
  private final String messageId;
  private final String correlationId;
  private final String clientId;

  public static MDCContext fromExchange(
      ServerWebExchange exchange, String app, String version, String profile) {
    var request = exchange.getRequest();

    String ts = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    String corrId =
        Optional.ofNullable(request.getHeaders().getFirst("X-CORRELATION-ID"))
            .filter(s -> !s.isEmpty())
            .orElseGet(() -> UUID.randomUUID().toString());
    String cId =
        Optional.ofNullable(request.getQueryParams().getFirst("client_id"))
            .filter(s -> !s.isEmpty())
            .orElse("NOT_REGISTERED");

    return new MDCContext(
        ts,
        getClientIP(exchange),
        app,
        "APPLICATION",
        profile,
        version,
        UUID.randomUUID().toString(),
        corrId,
        cId);
  }

  private static String getClientIP(ServerWebExchange exchange) {
    var headers = exchange.getRequest().getHeaders();
    String ip = headers.getFirst("X-Forwarded-For");
    if (isUnknown(ip)) ip = headers.getFirst("Proxy-Client-IP");
    if (isUnknown(ip)) ip = headers.getFirst("WL-Proxy-Client-IP");
    if (isUnknown(ip)) ip = headers.getFirst("HTTP_CLIENT_IP");
    if (isUnknown(ip)) ip = headers.getFirst("HTTP_X_FORWARDED_FOR");

    if (isUnknown(ip)) {
      ip =
          Optional.ofNullable(exchange.getRequest().getRemoteAddress())
              .map(addr -> addr.getAddress().getHostAddress())
              .orElse("unknown");
    }

    if (ip != null && !IP_ADDRESS_PATTERN.matcher(ip).matches() && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }

  private static boolean isUnknown(String s) {
    return s == null || s.isEmpty() || "unknown".equalsIgnoreCase(s);
  }

  public String getLogPrefix() {
    return String.format(
        "env=\"%s\" remoteAddress=\"%s\" correlationId=\"%s\" messageId=\"%s\" clientId=\"%s\" application=\"%s\" apiVersion=\"%s\" logType=\"%s\"",
        env, remoteAddress, correlationId, messageId, clientId, application, apiVersion, logType);
  }
}
