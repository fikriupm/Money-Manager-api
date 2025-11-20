package javaproject.moneymanager.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javaproject.moneymanager.dto.CategoryDTO;
import javaproject.moneymanager.entity.CategoryEntity;
import javaproject.moneymanager.entity.ProfileEntity;
import javaproject.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final ProfileService profileService;
  private final CategoryRepository categoryRepository;

  //save category
  public CategoryDTO savCategory(CategoryDTO categoryDTO){
    ProfileEntity profile = profileService.getCurrentProfile();
    if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
      throw new RuntimeException("Category with the same name already exists");
    }

    CategoryEntity newCategory = toEntity(categoryDTO, profile);
    newCategory = categoryRepository.save(newCategory);
    return toDTO(newCategory);
  }

  //get category for current user
  public List<CategoryDTO> getCategoriesForCurrentUser(){
    ProfileEntity profile = profileService.getCurrentProfile();
    List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
    return categories.stream().map(this::toDTO).toList();
  }

  //get category by type for current user
  public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
    ProfileEntity profile = profileService.getCurrentProfile();
    List<CategoryEntity> categories = categoryRepository.findByTypeAndProfileId(type, profile.getId());
    return categories.stream().map(this::toDTO).toList();
  }

  public CategoryDTO updateCategoryDTO(Long categoryId, CategoryDTO categoryDTO){
    ProfileEntity profile = profileService.getCurrentProfile();
    CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
      .orElseThrow(()-> new RuntimeException("Category not found or not accessible"));
    existingCategory.setName(categoryDTO.getName());
    existingCategory.setIcon(categoryDTO.getIcon());
    existingCategory = categoryRepository.save(existingCategory);
    return toDTO(existingCategory);

  }

  //helper method
  private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile){
    return CategoryEntity.builder()
      .name(categoryDTO.getName())
      .icon(categoryDTO.getIcon())
      .profile(profile)
      .type(categoryDTO.getType())
      .build();
  }

  private CategoryDTO toDTO(CategoryEntity entity){
    return CategoryDTO.builder()
      .id(entity.getId())
      .profileId(entity.getProfile() != null ? entity.getProfile().getId(): null)
      .name(entity.getName())
      .icon(entity.getIcon())
      .type(entity.getType())
      .createdAt(entity.getCreatedAt())
      .updatedAt(entity.getUpdatedAt())
      .build();
  }
  
}
