package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, body);
    }

    protected ResponseEntity<Object> get(String path, LocalDateTime start, LocalDateTime end, List<String> uris,
                                         Boolean unique) {
        return get(path, start, end, uris, unique);
    }
}
