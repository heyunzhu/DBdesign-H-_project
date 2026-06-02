package com.example.library.agent;

import com.example.library.common.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class DeepSeekClient {

    private final DeepSeekProperties properties;
    private final RestClient restClient;

    public DeepSeekClient(DeepSeekProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder.build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> chat(Map<String, Object> request) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException("DeepSeek API key is not configured");
        }
        return restClient.post()
                .uri(properties.getBaseUrl() + "/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);
    }
}
