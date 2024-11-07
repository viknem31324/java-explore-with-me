package ru.practicum.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ObjectCheckExistence {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final CompilationRepository compilationRepository;
    private final CommentRepository commentRepository;

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id %d не найден", id))
        );
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Категория с id %d не найдена", id))
        );
    }

    public void checkCategoryExistence(NewCategoryDto category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new ConflictException(String.format("Категория %s уже существует",
                    category.getName()));
        }
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Событие с id %d не найдено", id))
        );
    }

    public ParticipationRequest getRequest(Long id) {
        return requestRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Запрос с id %d не найден", id))
        );
    }

    public Compilation getCompilation(Long id) {
        return compilationRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Подборка с id %d не найдена", id))
        );
    }

    public void getDateTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException("Ошибка начального и конечного времени");
        }
    }

    public Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Комментарий с id %d не найден", id)));
    }

    public Long getCommentsCount(Long eventId) {
        return commentRepository.countAllByEventId(eventId);
    }

    public void checkUserIsAuthorComment(Long authorId, Long userId, Long commentId) {
        if (!Objects.equals(authorId, userId)) {
            throw new ValidationException(String.format(
                    "Пользователь с id %d не является автором комментария с id %d",
                    userId, commentId));
        }
    }

    public void checkComment(Event event, User user) {
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Событие должно быть со статусом PUBLISHED");
        }

        if (!Objects.equals(event.getInitiator().getId(), user.getId())) {
            ParticipationRequest result = requestRepository.findByRequesterIdAndEventId(user.getId(), event.getId())
                    .orElseThrow(() ->
                            new ValidationException(String.format("Пользователь с id %d не учавствует в событии с id %d",
                            user.getId(), event.getId())));
            if (result.getStatus() != RequestStatus.CONFIRMED) {
                throw new ValidationException(String.format("Пользователь с id %d не учавствует в событии с id %d",
                        user.getId(), event.getId()));
            }
        }

        Optional<Comment> foundComment = commentRepository.findByEventIdAndAuthorId(event.getId(), user.getId());
        if (foundComment.isPresent()) {
            throw new ConflictException(String.format("Пользователь с id='%s' уже оставлял комментарий к событию " +
                    "с id='%s'", user.getId(), event.getId()));
        }
    }
}
