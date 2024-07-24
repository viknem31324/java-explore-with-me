package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Statistic;

public class StatisticRepository extends JpaRepository<Statistic, Long> {
//    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(DISTINCT st.ip)) " +
//            "FROM Stats AS st " +
//            "WHERE st.timestamp BETWEEN :start AND :end " +
//            "GROUP BY st.app, st.uri " +
//            "ORDER BY COUNT(DISTINCT st.ip) DESC")
//    List<ViewStats> getAllUniqueStats(LocalDateTime start, LocalDateTime end);
//
//    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(st.ip)) " +
//            "FROM Stats AS st " +
//            "WHERE st.timestamp BETWEEN :start AND :end " +
//            "GROUP BY st.app, st.uri " +
//            "ORDER BY COUNT(st.ip) DESC")
//    List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end);
//
//    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(DISTINCT st.ip)) " +
//            "FROM Stats AS st " +
//            "WHERE st.timestamp BETWEEN :start AND :end " +
//            "AND st.uri IN :uris " +
//            "GROUP BY st.app, st.uri " +
//            "ORDER BY COUNT(DISTINCT st.ip) DESC")
//    List<ViewStats> getUniqueStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);
//
//    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(st.ip)) " +
//            "FROM Stats AS st " +
//            "WHERE st.timestamp BETWEEN :start AND :end " +
//            "AND st.uri IN :uris " +
//            "GROUP BY st.app, st.uri " +
//            "ORDER BY COUNT(st.ip) DESC")
//    List<ViewStats> getStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
