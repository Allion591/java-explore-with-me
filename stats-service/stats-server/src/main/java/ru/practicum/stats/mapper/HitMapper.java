package ru.practicum.stats.mapper;


import org.mapstruct.Mapper;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.model.Hit;

@Mapper(componentModel = "spring")
public interface HitMapper {
    Hit toHit(EndpointHit endpointHit);

    EndpointHit toEndpointHit(Hit hit);
}