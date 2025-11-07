package ru.practicum.main.service.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.dto.compilation.NewCompilationDto;
import ru.practicum.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.main.exception.notFound.CompilationNotFoundException;
import ru.practicum.main.mapper.CompilationMapper;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.service.interfaces.CompilationService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание новой подборки: {}", newCompilationDto.getTitle());

        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
        }
        compilation.setEvents(events);

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Создана подборка с id: {}", savedCompilation.getId());

        return compilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        log.info("Удаление подборки с id: {}", compilationId);

        if (!compilationRepository.existsById(compilationId)) {
            log.error("Подборка не найдена");
            throw new CompilationNotFoundException(compilationId);
        }

        compilationRepository.deleteById(compilationId);
        log.info("Подборка с id {} удалена", compilationId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateRequest) {
        log.info("Обновление подборки с id: {}", compilationId);

        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new CompilationNotFoundException(compilationId));

        compilationMapper.updateCompilationFromRequest(updateRequest, compilation);

        // Обновление событий, если указаны
        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findPublishedEventsByIds(updateRequest.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);

        log.info("Подборка с id {} обновлена", compilationId);
        return compilationMapper.toCompilationDto(updatedCompilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение закрепленных подборок: {}, from: {}, size: {}", pinned, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long compilationId) {
        log.info("Получение подборки по id: {}", compilationId);

        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new CompilationNotFoundException(compilationId));

        return compilationMapper.toCompilationDto(compilation);
    }
}