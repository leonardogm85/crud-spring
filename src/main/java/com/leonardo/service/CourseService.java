package com.leonardo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.leonardo.dto.CourseDTO;
import com.leonardo.dto.PageDTO;
import com.leonardo.dto.mapper.CourseMapper;
import com.leonardo.exception.RecordNotFoundException;
import com.leonardo.model.Course;
import com.leonardo.repository.CourseRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Validated
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    public PageDTO<CourseDTO> list(@PositiveOrZero int pageNumber, @Positive @Max(100) int pageSize) {
        Page<Course> page = courseRepository
                .findAll(PageRequest.of(pageNumber, pageSize));
        List<CourseDTO> dtos = page
                .get()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
        return new PageDTO<CourseDTO>(
                dtos,
                page.getTotalElements(),
                page.getTotalPages());
    }

    public CourseDTO loadById(@NonNull @Positive Long id) {
        return courseRepository
                .findById(id)
                .map(courseMapper::toDTO)
                .orElseThrow(() -> new RecordNotFoundException(id));
    }

    public CourseDTO create(@NonNull @Valid CourseDTO dto) {
        return courseMapper.toDTO(courseRepository.save(courseMapper.toEntity(dto)));
    }

    public CourseDTO update(@NonNull @Positive Long id, @NonNull @Valid CourseDTO dto) {
        return courseRepository.findById(id)
                .map(entity -> {
                    entity.setName(dto.name());
                    entity.setCategory(courseMapper.convertCategoryValue(dto.category()));
                    Course course = courseMapper.toEntity(dto);
                    entity.getLessons().clear();
                    course.getLessons().forEach(entity.getLessons()::add);
                    return courseMapper.toDTO(courseRepository.save(entity));
                }).orElseThrow(() -> new RecordNotFoundException(id));
    }

    public void delete(@NonNull @Positive Long id) {
        courseRepository.delete(courseRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(id)));
    }

}
