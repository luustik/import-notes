package dev.newsystem.service.impl;

import dev.newsystem.dto.request.OldClientNotesRequest;
import dev.newsystem.dto.response.OldClientResponse;
import dev.newsystem.dto.response.OldNoteResponse;
import dev.newsystem.service.OldSystemClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

import static dev.newsystem.util.Constants.GET_CLIENTS_OLD_SYSTEM_ENDPOINT;
import static dev.newsystem.util.Constants.GET_NOTES_OLD_SYSTEM_ENDPOINT;

@Service
public class OldSystemClientImpl implements OldSystemClient {

    private final RestClient restClient;

    public OldSystemClientImpl(
            @Value("${old-system.base-url}") String baseUrl,
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public List<OldClientResponse> getClients() {
        return restClient.post()
                .uri(GET_CLIENTS_OLD_SYSTEM_ENDPOINT)
                .body(Collections.emptyMap())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public List<OldNoteResponse> getNotes(OldClientNotesRequest oldClientNotesRequest) {
        return restClient.post()
                .uri(GET_NOTES_OLD_SYSTEM_ENDPOINT)
                .body(oldClientNotesRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
