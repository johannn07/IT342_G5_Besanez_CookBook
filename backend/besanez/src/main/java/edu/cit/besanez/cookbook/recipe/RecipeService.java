package edu.cit.besanez.cookbook.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.besanez.cookbook.collection.CollectionEntity;
import edu.cit.besanez.cookbook.shared.exception.ResourceNotFoundException;
import edu.cit.besanez.cookbook.user.UserEntity;
import edu.cit.besanez.cookbook.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @Transactional
    public RecipeResponseDTO createRecipe(long userId, RecipeRequestDTO requestDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        RecipeEntity recipe = RecipeEntity.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .prepTimeMinutes(requestDTO.getPrepTimeMinutes())
                .cookTimeMinutes(requestDTO.getCookTimeMinutes())
                .totalTimeMinutes(requestDTO.getTotalTimeMinutes())
                .notes(requestDTO.getNotes())
                .imageUrl(requestDTO.getImageUrl())
                .isPublic(requestDTO.isPublic())
                .user(user)
                .build();

        RecipeEntity saved = recipeRepository.save(recipe);

        return convertToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public RecipeResponseDTO getRecipeById(long userId, Long recipeId) {
        RecipeEntity recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Recipe not found with id: " + recipeId));

        return convertToResponseDTO(recipe);
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponseDTO> getAllRecipesByUser(long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return recipeRepository.findByUserId(userId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponseDTO> getRecipesByCollection(long userId, Long collectionId,
            Pageable pageable) {
        return recipeRepository.findByCollectionIdAndUserId(collectionId, userId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponseDTO> searchUserRecipes(long userId, String name, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return recipeRepository.findByUserIdAndNameContainingIgnoreCase(userId, name, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponseDTO> searchPublicRecipes(String name, Pageable pageable) {
        return recipeRepository.findByIsPublicTrueAndNameContainingIgnoreCase(name, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public RecipeResponseDTO updateRecipe(long userId, Long recipeId, RecipeRequestDTO requestDTO) {
        RecipeEntity recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Recipe not found with id: " + recipeId));

        recipe.setName(requestDTO.getName());
        recipe.setDescription(requestDTO.getDescription());
        recipe.setPrepTimeMinutes(requestDTO.getPrepTimeMinutes());
        recipe.setCookTimeMinutes(requestDTO.getCookTimeMinutes());
        recipe.setTotalTimeMinutes(requestDTO.getTotalTimeMinutes());
        recipe.setNotes(requestDTO.getNotes());
        recipe.setImageUrl(requestDTO.getImageUrl());
        recipe.setPublic(requestDTO.isPublic());

        RecipeEntity updated = recipeRepository.save(recipe);

        return convertToResponseDTO(updated);
    }

    @Transactional
    public void deleteRecipe(long userId, Long recipeId) {
        // Fetch the recipe along with its collections to avoid lazy loading issues
        RecipeEntity recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + recipeId));

        // Remove the recipe from all collections (this updates the join table)
        for (CollectionEntity collection : new ArrayList<>(recipe.getCollections())) {
            collection.getRecipes().remove(recipe);
        }
        recipe.getCollections().clear(); // optional, ensures the recipe’s side is empty

        // Now it’s safe to delete the recipe (cascades will handle ingredients &
        // instructions)
        recipeRepository.delete(recipe);
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private RecipeResponseDTO convertToResponseDTO(RecipeEntity recipe) {
        return RecipeResponseDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .totalTimeMinutes(recipe.getTotalTimeMinutes())
                .notes(recipe.getNotes())
                .imageUrl(recipe.getImageUrl())
                .isPublic(recipe.isPublic())
                .userId(recipe.getUser().getId())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }
}