package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.client.StatisticClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.enums.State;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.util.ObjectCheckExistence;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static ru.practicum.event.mapper.EventMapper.EVENT_MAPPER;
import static ru.practicum.util.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final ObjectCheckExistence checkExistence;
    private final LocationRepository locationRepository;
    private final StatisticClient statisticClient;

    @Override
    public List<EventFullDto> findAllEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                   Pageable pageable) {
        Map<Long, Long> views;
        List<EventFullDto> findEvents;
        
        if (states == null && rangeStart == null && rangeEnd == null) {
            List<Event> events = eventRepository.findAll(pageable).stream().toList();
            views = getViewsForEvents(events);
                  
            findEvents = events
                    .stream()
                    .map(item -> EVENT_MAPPER.toEventFullDto(item, views.get(item.getId())))
                    .collect(toList());
            log.debug("Найдены события: {}", findEvents);
            return findEvents;
        }

        if (states == null) {
            states = Stream.of(State.values()).collect(toList());
        }

        rangeStart = rangeStart == null ? CURRENT_TIME.minusYears(5) : rangeStart;
        rangeEnd = rangeEnd == null ? CURRENT_TIME.plusYears(5) : rangeEnd;

        checkExistence.getDateTime(rangeStart, rangeEnd);
        List<Event> events = eventRepository.findByParams(users, states, categories, rangeStart, rangeEnd, pageable);
        views = getViewsForEvents(events);
        
        findEvents = events.stream()
                .map(item -> EVENT_MAPPER.toEventFullDto(item, views.get(item.getId())))
                .collect(toList());
        log.debug("Найдены события: {}", findEvents);
        return findEvents;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = checkExistence.getEvent(eventId);

        if (request.getEventDate() != null) {
            checkStartTime(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case REJECT_EVENT:
                    checkEventStatus(event, request, List.of(State.PENDING));
                    event.setState(State.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    checkEventStatus(event, request, List.of(State.PENDING));
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(CURRENT_TIME);
                    break;
            }
        }
        
        Event currentEvent = eventRepository.save(updateEvent(event, request));
        Long views = getViewsForEvent(currentEvent);
        EventFullDto updatedEvent = EVENT_MAPPER.toEventFullDto(currentEvent, views);
        log.debug("Обновленное событие: {}", updatedEvent);
        return updatedEvent;
    }

    @Override
    public List<EventShortDto> findAllPublicEventsByPublic(String text,
                                                     List<Long> categories,
                                                     Boolean paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable,
                                                     Sort sort,
                                                     Pageable pageable,
                                                     HttpServletRequest request) {
        List<EventShortDto> findEvents;
        LocalDateTime start = rangeStart == null ? CURRENT_TIME : rangeStart;
        LocalDateTime end = rangeEnd == null ? CURRENT_TIME.plusYears(15) : rangeEnd;
        checkExistence.getDateTime(start, end);

        List<Event> events = eventRepository.findByParamsOrderByDate(
                text == null ? "" : text.toLowerCase(),
                List.of(State.PUBLISHED),
                categories,
                paid,
                start,
                end,
                pageable);
        List<Event> eventList = confirmedRequests(events);

        if (onlyAvailable) {
            findEvents = eventList.stream()
                    .filter(event -> event.getParticipantLimit() <= event.getConfirmedRequests())
                    .map(EVENT_MAPPER::toEventShortDto)
                    .collect(toList());
        } else {
            findEvents = eventList.stream()
                    .map(EVENT_MAPPER::toEventShortDto)
                    .collect(toList());
        }

        List<EndpointHit> endpointHitList = new ArrayList<>();
        findEvents.forEach(item -> {
            EndpointHit endpointHit = EndpointHit.builder()
                    .app(APP_NAME)
                    .ip(request.getRemoteAddr())
                    .uri(request.getRequestURI())
                    .timestamp(CURRENT_TIME)
                    .build();
            endpointHitList.add(endpointHit);
        });

        if (!endpointHitList.isEmpty()) {
            statisticClient.saveHit(endpointHitList.getFirst());
        }

        return findEvents;
    }

    @Transactional
    @Override
    public EventFullDto findPublicEventById(Long id, HttpServletRequest request) {
        Event event = checkExistence.getEvent(id);

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id %d пока не опубликовано.", event.getId()));
        }

        EndpointHit endpointHit = EndpointHit.builder()
                .app(APP_NAME)
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(CURRENT_TIME)
                .build();
        statisticClient.saveHit(endpointHit);

        List<ViewStats> viewStatsList = statisticClient.getStatistic(List.of("/events/" + id.toString()), true);
        long hits = viewStatsList.stream()
                .filter(s -> Objects.equals(s.getUri(), (request.getRequestURI() + "/" + event.getId())))
                .count() + 1;

        event.setConfirmedRequests((long) requestRepository.findAllByEventIdInAndStatus(List.of(id),
                RequestStatus.CONFIRMED).size());
        Event currentEvent = eventRepository.save(event);
        return EVENT_MAPPER.toEventFullDto(currentEvent, hits);
    }

    @Override
    @Transactional
    public EventFullDto createEventByUser(Long userId, NewEventDto newEventDto) {
        User user = checkExistence.getUser(userId);

        if (newEventDto.getEventDate().isBefore(CURRENT_TIME.plusHours(2))) {
            throw new ValidationException("Время старта события должно быть позже текущего времени минимум на 2 часа");
        }

        Event event = EVENT_MAPPER.toEvent(newEventDto);
        event.setState(State.PENDING);
        event.setCreatedOn(CURRENT_TIME);
        event.setConfirmedRequests(0L);

        Category category = checkExistence.getCategory(newEventDto.getCategory());

        event.setCategory(category);
        event.setInitiator(user);
        Event currentEvent = eventRepository.save(event);
        EventFullDto eventSaved = EVENT_MAPPER.toEventFullDto(currentEvent, 0L);
        log.debug("Создано новое событие: {}", eventSaved);
        return eventSaved;
    }

    @Override
    public List<EventFullDto> findPrivateAllEventsByUser(Long userId, Pageable pageable) {
        checkExistence.getUser(userId);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        Map<Long, Long> views = getViewsForEvents(events);
        
        List<EventFullDto> listEvents = events
                .stream()
                .map(item -> EVENT_MAPPER.toEventFullDto(item, views.get(item.getId())))
                .collect(toList());
        log.debug("Найдены события: {}", listEvents);
        return listEvents;
    }

    @Override
    public EventFullDto findPrivateEventForUserByEventId(Long userId, Long eventId) {
        Event event = checkExistence.getEvent(eventId);
        User user = checkExistence.getUser(userId);

        checkEventOwner(event, user);
        Long views = getViewsForEvent(event);
        EventFullDto findEvent = EVENT_MAPPER.toEventFullDto(event, views);
        log.debug("Найдено событие: {}", findEvent);
        return findEvent;
    }

    @Override
    @Transactional
    public EventFullDto updatePrivateEventByUser(Long userId,  Long eventId, UpdateEventUserRequest request) {
        Event event = checkExistence.getEvent(eventId);
        User user = checkExistence.getUser(userId);
        checkEventOwner(event, user);
        checkEventStatus(event, request, List.of(State.PENDING, State.CANCELED));
        checkDate(request);

        if (StateAction.SEND_TO_REVIEW == request.getStateAction()) {
            event.setState(State.PENDING);
        }
        if (StateAction.CANCEL_REVIEW == request.getStateAction()) {
            event.setState(State.CANCELED);
        }
        
        Event currentEvent = eventRepository.save(updateEvent(event, request));
        Long views = getViewsForEvent(currentEvent);
        EventFullDto updatedEvent = EVENT_MAPPER.toEventFullDto(currentEvent, views);
        log.debug("Обновлено событие: {}", updatedEvent);
        return updatedEvent;
    }

    private Event updateEvent(Event event, UpdateEventRequest request) {
        return constructEvent(event, request);
    }

    private List<Event> confirmedRequests(List<Event> events) {
        List<Long> numberEvents = events
                .stream()
                .map(Event::getId)
                .collect(toList());

        events.forEach(event -> event.setConfirmedRequests((long) requestRepository.findAllByEventIdInAndStatus(
                        new ArrayList<>(numberEvents), RequestStatus.CONFIRMED).size()));

        return events.stream()
                .map(eventRepository::save)
                .collect(toList());
    }

    private Long getViewsForEvent(Event event) {
        Long eventId = event.getId();
        List<String> eventIds = List.of("/events/" + eventId);
        List<ViewStats> viewStatsList = statisticClient.getStatistic(eventIds, false);
        if (!viewStatsList.isEmpty()) {
            return viewStatsList.getFirst().getHits();
        }
        return 0L;
    }

    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        List<Long> numberEvents = events
                .stream()
                .map(Event::getId)
                .collect(toList());
        List<String> eventIds = numberEvents.stream()
                .map(item -> "/events/" + item)
                .collect(toList());

        List<ViewStats> viewStatsList = statisticClient.getStatistic(eventIds, false);
  
        if (viewStatsList != null && !viewStatsList.isEmpty()) {
            return viewStatsList.stream()
                    .collect(Collectors.toMap(this::getEventIdFromURI, ViewStats::getHits));
        }
        
        return Collections.emptyMap();
    }

    private Long getEventIdFromURI(ViewStats viewStats) {
        return Long.parseLong(viewStats.getUri().substring(viewStats.getUri().lastIndexOf("/") + 1));
    }

    private void checkStartTime(LocalDateTime time) {
        if (CURRENT_TIME.isAfter(time)) {
            throw new ValidationException("Некорректное время начала");
        }
    }

    private void checkDate(UpdateEventRequest request) {
        if ((request != null && request.getEventDate() != null) &&
                !request.getEventDate().isAfter(CURRENT_TIME.plusHours(2))) {
            throw new ValidationException("Время старта события должно быть позже текущего времени минимум на 2 часа");
        }
    }

    private void checkEventStatus(Event event, UpdateEventRequest request, List<State> allowedState) {
        boolean valid = false;
        for (State state : allowedState) {
            if (event.getState() == state) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new ConflictException(String.format("Событие может быть опубликовано только с одним из статусов %s",
                    allowedState) + request.getStateAction());
        }
    }

    private void checkEventOwner(Event event, User user) {
        if (!event.getInitiator().equals(user)) {
            throw new NotFoundException(String.format("Пользователь %s не является владельцем события %d",
                    user.getName(), event.getId()));
        }
    }

    private void checkDescription(UpdateEventRequest request) {
        if (request.getDescription().length() > DESCRIPTION_MAX || request.getDescription().length() < DESCRIPTION_MIN) {
            throw new ValidationException("Должно быть не менее " + DESCRIPTION_MIN + " и не более " + DESCRIPTION_MAX);
        }
    }

    private void checkAnnotaion(UpdateEventRequest request) {
        if (request.getAnnotation().length() > ANNOTATION_MAX || request.getAnnotation().length() < ANNOTATION_MIN) {
            throw new ValidationException("Должно быть не менее " + ANNOTATION_MIN + " и не более " + ANNOTATION_MAX);
        }
    }

    private void checkTitle(UpdateEventRequest request) {
        if (request.getTitle().length() < TITLE_MIN || request.getTitle().length() > TITLE_MAX) {
            throw new ValidationException("Должно быть не менее " + TITLE_MIN + " и не более " + TITLE_MAX);
        }
    }

    private Event constructEvent(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null) {
            checkAnnotaion(request);
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(checkExistence.getCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            checkDescription(request);
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            checkTitle(request);
            event.setTitle(request.getTitle());
        }
        return event;
    }
}