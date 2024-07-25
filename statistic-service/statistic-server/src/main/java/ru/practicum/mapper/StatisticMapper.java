package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EndpointHit;
import ru.practicum.model.Statistic;

@UtilityClass
public class StatisticMapper {
    public Statistic mapToStatistic(EndpointHit endpointHit) {
        return Statistic.builder()
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .app(endpointHit.getApp())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }
}
