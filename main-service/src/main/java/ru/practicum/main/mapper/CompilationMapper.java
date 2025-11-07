package ru.practicum.main.mapper;

import org.mapstruct.*;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.dto.compilation.NewCompilationDto;
import ru.practicum.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.main.model.Compilation;
import java.util.Collections;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {
    @Mapping(target = "events", source = "events")
    CompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto newCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompilationFromRequest(UpdateCompilationRequest updateCompilationRequest,
                                      @MappingTarget Compilation compilation);

    @AfterMapping
    default void setDefaultsForNewCompilation(@MappingTarget Compilation compilation, NewCompilationDto newCompilationDto) {
        if (compilation.getEvents() == null) {
            compilation.setEvents(Collections.emptySet());
        }
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }
    }
}