package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

@Mapper
public interface EventMapper {
    EventMapper EVENT_MAPPER = Mappers.getMapper(EventMapper.class);

    @Mapping(source = "category", target = "category.id")
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "views", source = "hits")
    EventFullDto toEventFullDto(Event event, Long hits);

    EventShortDto toEventShortDto(Event event);
}
