package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StatisticService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statsService;
    private final Logger log = LoggerFactory.getLogger(StatisticController.class);
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    public void saveHit(@Valid @RequestBody EndpointHit hit) {
        log.info("Получена статистика: {}", hit);
        statsService.saveHit(hit);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(@RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime start,
                                                    @RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime end,
                                                    @RequestParam(required = false) List<String> uris,
                                                    @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получен запрос на получение статистики");

        return null;
    }
}
