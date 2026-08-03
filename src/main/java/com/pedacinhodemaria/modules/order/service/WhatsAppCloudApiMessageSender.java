package com.pedacinhodemaria.modules.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Adapter concreto do port WhatsAppMessageSender, usando a WhatsApp Cloud
 * API (Meta) diretamente via HTTP — sem SDK de terceiros, só RestTemplate.
 *
 * Se o projeto já usa outro provedor (Twilio, Z-API etc.), troque só esta
 * classe por outra que implemente WhatsAppMessageSender — nada em
 * SendOrderReadyWhatsAppMessageUseCase ou no Controller precisa mudar; é
 * exatamente o ponto de extensão que a interface existe para proteger.
 *
 * Requer as propriedades abaixo em application.yml/properties (ver
 * README-whatsapp-setup.md para o passo a passo de onde obter cada uma):
 *
 *   app.whatsapp.api-url=https://graph.facebook.com/v19.0
 *   app.whatsapp.phone-number-id=<Phone Number ID do WhatsApp Business>
 *   app.whatsapp.access-token=<token de acesso permanente do Meta App>
 */
@Component
@Slf4j
public class WhatsAppCloudApiMessageSender implements WhatsAppMessageSender {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String phoneNumberId;
    private final String accessToken;

    public WhatsAppCloudApiMessageSender(
            RestTemplate restTemplate,
            @Value("${app.whatsapp.api-url}") String apiUrl,
            @Value("${app.whatsapp.phone-number-id}") String phoneNumberId,
            @Value("${app.whatsapp.access-token}") String accessToken
    ) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
    }

    @Override
    public void sendMessage(String phoneNumber, String message) {
        String url = "%s/%s/messages".formatted(apiUrl, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", toE164(phoneNumber),
                "type", "text",
                "text", Map.of("body", message)
        );

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception ex) {
            // Falha ao falar com o provedor de WhatsApp não deve derrubar a
            // requisição do funcionário que clicou no Dashboard — ele já viu
            // a ação disparada; o erro fica logado para investigação, não
            // propagado como 500 para a UI.
            log.error("Falha ao enviar mensagem de WhatsApp para {}: {}", phoneNumber, ex.getMessage(), ex);
        }
    }

    /** Números são persistidos só com dígitos (ver PhoneNumberNormalizer) — a Cloud API exige E.164 com DDI. */
    private String toE164(String phoneNumber) {
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        return digitsOnly.startsWith("55") ? "+" + digitsOnly : "+55" + digitsOnly;
    }
}