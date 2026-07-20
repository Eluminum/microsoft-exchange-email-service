package co.eluminum.api.email.dto;

public record HealthResponse(
    String status, String apiVersion, String timestamp, Boolean exchangeConnectivity) {}
