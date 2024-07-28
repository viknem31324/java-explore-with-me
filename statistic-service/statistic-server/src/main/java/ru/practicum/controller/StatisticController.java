package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StatisticService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.helpers.Constants.FORMAT;

@Validated
@RestController
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statisticService;
    private final Logger log = LoggerFactory.getLogger(StatisticController.class);

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@Valid @RequestBody EndpointHit hit) {
        log.info("Получена статистика: {}", hit);
        statisticService.saveHit(hit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStatistic(@RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime start,
                                        @RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime end,
                                        @RequestParam List<String> uris,
                                        @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получен запрос на получение статистики");

        return statisticService.getStatistic(start, end, uris, unique);
    }
}
