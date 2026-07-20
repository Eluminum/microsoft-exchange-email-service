package co.eluminum.api.email.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import co.eluminum.api.email.enums.EmailContentType;
import co.eluminum.api.email.enums.EmailPriority;

public record SendEmailRequest(
    @NotEmpty(message = "Recipient list cannot be empty") List<String> to,
    List<String> cc,
    @NotNull(message = "Subject is required") String subject,
    String body,
    @NotNull(message = "Message type is required") EmailContentType messageType,
    EmailPriority priority) {}
