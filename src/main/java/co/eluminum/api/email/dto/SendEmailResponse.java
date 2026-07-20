package co.eluminum.api.email.dto;

import jakarta.validation.constraints.NotNull;

public record SendEmailResponse(
    @NotNull(message = "Delivery ticket must be generated") String deliveryTicket,
    @NotNull(message = "Status cannot be null") String status) {}
