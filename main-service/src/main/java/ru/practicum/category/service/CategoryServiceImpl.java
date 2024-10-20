package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.util.ObjectCheckExistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.category.mapper.CategoryMapper.CATEGORY_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        checkExistence.checkCategoryExistence(newCategoryDto);

        Category category = categoryRepository.save(CATEGORY_MAPPER.toCategory(newCategoryDto));
        CategoryDto createdCategory = CATEGORY_MAPPER.toCategoryDto(category);
        log.debug("Создана новая категория: {}", createdCategory);
        return createdCategory;
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        log.debug("Обновление категории с id: {}: {}", catId, newCategoryDto);
        Category category = checkExistence.getCategory(catId);
        Optional<Category> existCategory = categoryRepository.findByName(newCategoryDto.getName());

        if (existCategory.isPresent() && !category.getName().equals(newCategoryDto.getName())) {
            throw new ConflictException(String.format("Категория с названием %s уже существует",
                    newCategoryDto.getName()));
        }

        category.setName(newCategoryDto.getName());
        Category updateCategory = categoryRepository.save(category);
        log.debug("Обновленная категория: {}", updateCategory);
        return CATEGORY_MAPPER.toCategoryDto(updateCategory);
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConflictException("Категория не может быть удалена, пока в ней есть события");
        }
        checkExistence.getCategory(catId);
        categoryRepository.deleteById(catId);
        log.debug("Удалена категория с id {}", catId);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        List<CategoryDto> categories = categoryRepository.findAll(pageable).stream()
                .map(CATEGORY_MAPPER::toCategoryDto)
                .collect(Collectors.toList());
        log.debug("Получен список категорий с {} по {}", from, size);
        return categories;
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        CategoryDto category = CATEGORY_MAPPER.toCategoryDto(checkExistence.getCategory(catId));
        log.debug("Найдена категория: {}", category);
        return category;
    }
}
