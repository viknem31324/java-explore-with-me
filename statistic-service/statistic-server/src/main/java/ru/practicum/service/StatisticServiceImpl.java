package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.mapper.StatisticMapper;
import ru.practicum.model.Statistic;
import ru.practicum.repository.StatisticRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final StatisticRepository statisticRepository;
    private final Logger log = LoggerFactory.getLogger(StatisticServiceImpl.class);

    @Transactional
    @Override
    public void saveHit(EndpointHit hit) {
        Statistic statistic = StatisticMapper.mapToStatistic(hit);
        log.debug("Собрана статистика: {}", statistic);
        statisticRepository.save(statistic);
    }


    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.debug("Получен запрос с параметрами: {} {} {} {}", start, end, uris, unique);
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statisticRepository.getAllUniqueStats(start, end);
            } else {
                return statisticRepository.getAllStats(start, end);
            }
        } else {
            if (unique) {
                return statisticRepository.getUniqueStatsByUris(start, end, uris);
            } else {
                return statisticRepository.getStatsByUris(start, end, uris);
            }
        }
    }
}
