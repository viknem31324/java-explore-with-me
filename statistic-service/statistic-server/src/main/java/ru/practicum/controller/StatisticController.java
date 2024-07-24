package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
public class StatisticController {
    private final Logger log = LoggerFactory.getLogger(StatisticController.class);

//    @PostMapping("/hit")
//    public void saveHit(@Valid @RequestBody EndpointHit hit) {
//        log.info("---START POST HIT ENDPOINT---");
//        statsService.postHit(hit);
//    }

//    @GetMapping("/stats")
//    public ResponseEntity<List<ViewStats>> getStats(@RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime start,
//                                                    @RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime end,
//                                                    @RequestParam(required = false) List<String> uris,
//                                                    @RequestParam(defaultValue = "false") Boolean unique) {
//        log.info("---START GET STATS ENDPOINT---");
//        if (start.isAfter(end)) {
//            throw new ValidationException(
//                    String.format("Unexpected time interval: start %s; end %s", start, end));
//        }
//        return new ResponseEntity<>(statsService.getStats(start, end, uris, unique), HttpStatus.OK);
//    }
}
