package co.eluminum.api.email.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

  @Bean
  DefaultAzureCredential defaultAzureCredential() {
    return new DefaultAzureCredentialBuilder().build();
  }
}
