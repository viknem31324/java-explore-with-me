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

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final StatisticRepository statisticRepository;
    private final Logger log = LoggerFactory.getLogger(StatisticServiceImpl.class);

    @Transactional
    @Override
    public void saveHit(EndpointHit hit) {
        Statistic statistic = StatisticMapper.mapToStatistic(hit);
        Statistic saveStatistic = statisticRepository.save(statistic);
        log.debug("Собрана статистика: {}", saveStatistic);
    }


    @Override
    public List<ViewStats> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.debug("Получен запрос с параметрами: {} {} {} {}", start, end, uris, unique);
//        if (uris == null || uris.isEmpty()) {
//            if (unique) {
//                return statisticRepository.getAllUniqueStatistic(start, end);
//            } else {
//                return statisticRepository.getAllStatistic(start, end);
//            }
//        } else {
//            if (unique) {
//                return statisticRepository.getUniqueStatisticByUris(start, end, uris);
//            } else {
//                return statisticRepository.getStatisticByUris(start, end, uris);
//            }
//        }
        return null;
    }
}
