package ru.practicum.service;

import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticService {
    void saveHit(EndpointHit hit);

    List<ViewStats> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
