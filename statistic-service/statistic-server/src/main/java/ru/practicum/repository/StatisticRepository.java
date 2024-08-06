package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.ViewStats;
import ru.practicum.model.Statistic;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticRepository extends JpaRepository<Statistic, Long> {
    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(DISTINCT st.ip)) " +
            "FROM Statistic AS st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(DISTINCT st.ip) DESC")
    List<ViewStats> getAllUniqueStatistic(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(st.ip)) " +
            "FROM Statistic AS st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(st.ip) DESC")
    List<ViewStats> getAllStatistic(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(DISTINCT st.ip)) " +
            "FROM Statistic AS st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "AND st.uri IN :uris " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(DISTINCT st.ip) DESC")
    List<ViewStats> getUniqueStatisticByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(st.ip)) " +
            "FROM Statistic AS st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "AND st.uri IN :uris " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(st.ip) DESC")
    List<ViewStats> getStatisticByUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
