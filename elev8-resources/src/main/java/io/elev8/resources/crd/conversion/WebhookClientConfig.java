package io.elev8.resources.crd.conversion;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * WebhookClientConfig contains the information to make a TLS connection with the webhook.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookClientConfig {
    private String url;
    private ServiceReference service;
    private String caBundle;
}
