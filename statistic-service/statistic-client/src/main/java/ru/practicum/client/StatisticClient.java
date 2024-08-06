package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatisticClient {
    protected final RestTemplate rest;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatisticClient(@Value("${statistic-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void saveHit(EndpointHit endpointHit) {
        rest.postForEntity("/hit", new HttpEntity<>(endpointHit), Void.class);
    }

    public List<ViewStats> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris,
                                               Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", String.join(",", uris),
                "unique", unique
        );
        return rest.exchange("/stats?start={start}&end={end}&uris={uris}&unique={unique}", HttpMethod.GET,
                null, new ParameterizedTypeReference<List<ViewStats>>() {
                }, parameters).getBody();
    }
}
