package co.eluminum.api.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;

import reactor.core.publisher.Mono;

@Configuration
public class GraphClientConfig {

	@Value("${azure.graph.base-url}")
    private String baseUrl;

    @Value("${azure.graph.scopes}")
    private String scope;
    
    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    WebClient graphWebClient(WebClient.Builder builder, DefaultAzureCredential credential) {
        return builder
            .baseUrl(baseUrl)
            .filter((request, next) -> 
                Mono.fromCompletionStage(
                    credential.getToken(new TokenRequestContext().addScopes(scope)).toFuture()
                )
                .flatMap(token -> {
                    ClientRequest filtered = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getToken())
                        .build();
                    return next.exchange(filtered);
                })
            )
            .build();
    }
}