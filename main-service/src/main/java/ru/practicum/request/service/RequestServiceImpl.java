package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.util.ObjectCheckExistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.event.enums.State.PUBLISHED;
import static ru.practicum.request.enums.RequestStatus.CONFIRMED;
import static ru.practicum.request.enums.RequestStatus.PENDING;
import static ru.practicum.request.enums.RequestStatus.REJECTED;
import static ru.practicum.request.mapper.RequestMapper.REQUEST_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public List<ParticipationRequestDto> findByRequestorId(Long userId) {
        User user = checkExistence.getUser(userId);
        return requestRepository.findAllByRequester(user).stream()
                .map(REQUEST_MAPPER::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = checkExistence.getUser(userId);
        Event event = checkExistence.getEvent(eventId);

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.getRequestModeration() ? PENDING : CONFIRMED)
                .build();

        Optional<ParticipationRequest> req = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (req.isPresent()) {
            throw new ConflictException("Запрос уже существует!");
        }
        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Пользователь является создателем события");
        }
        if (event.getState() != PUBLISHED) {
            throw new ConflictException("Событие не опубликовано!");
        }
        if (event.getParticipantLimit() > 0) {
            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Превышен лимит участников");
            }
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        if (request.getStatus() == CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return REQUEST_MAPPER.toParticipationRequestDto(requestRepository.save(request));
    }


    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = checkExistence.getUser(userId);
        ParticipationRequest request = checkExistence.getRequest(requestId);

        if (!request.getRequester().equals(user)) {
            throw new ConflictException("Можно отменить только свой запрос!");
        }

        request.setStatus(RequestStatus.CANCELED);
        return REQUEST_MAPPER.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsEventByUser(Long userId, Long eventId) {
        User user = checkExistence.getUser(userId);
        Event event = checkExistence.getEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Можно отменить только свой запрос!");
        }
        return requestRepository.findAllByEvent(event)
                .stream()
                .map(REQUEST_MAPPER::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsByUser(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest updateRequest) {
        User user = checkExistence.getUser(userId);
        Event event = checkExistence.getEvent(eventId);

        if (!Objects.equals(event.getInitiator().getId(), user.getId())) {
            throw new ValidationException("Только создатель события может принять запрос на участие!");
        }
        if (event.getParticipantLimit() <= event.getConfirmedRequests() && event.getParticipantLimit() != 0) {
            throw new ConflictException("Превышен лимит участников");
        }

        List<ParticipationRequest> requestList = requestRepository.findAllByIdIn(updateRequest.getRequestIds());

        if (requestList.size() != updateRequest.getRequestIds().size()) {
            throw new ConflictException("Событие не найдено");
        }

        switch (updateRequest.getStatus()) {
            case CONFIRMED:
                return updateConfirmedStatus(requestList, event);
            case REJECTED:
                return updateRejectedStatus(requestList);
            default:
                throw new ValidationException("Не корректный статус");
        }
    }

    private EventRequestStatusUpdateResult updateConfirmedStatus(List<ParticipationRequest> requestList, Event event) {
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requestList) {
            if (!request.getStatus().equals(PENDING)) {
                throw new ConflictException("Событие не имеет статус PENDING");
            }
            if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                request.setStatus(REJECTED);
                rejected.add(REQUEST_MAPPER.toParticipationRequestDto(request));
            } else {
                request.setStatus(CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmed.add(REQUEST_MAPPER.toParticipationRequestDto(request));
            }
            requestRepository.save(request);
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    private EventRequestStatusUpdateResult updateRejectedStatus(List<ParticipationRequest> requestList) {
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requestList) {
            if (!request.getStatus().equals(PENDING)) {
                throw new ConflictException("Событие не имеет статус PENDING");
            }
            request.setStatus(REJECTED);
            requestRepository.save(request);
            rejected.add(REQUEST_MAPPER.toParticipationRequestDto(request));
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(rejected)
                .build();
    }
}
