package edu.cit.besanez.cookbook.share;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.besanez.cookbook.collection.CollectionRepository;
import edu.cit.besanez.cookbook.ingredient.IngredientEntity;
import edu.cit.besanez.cookbook.ingredient.IngredientRepository;
import edu.cit.besanez.cookbook.instruction.InstructionEntity;
import edu.cit.besanez.cookbook.instruction.InstructionRepository;
import edu.cit.besanez.cookbook.recipe.RecipeEntity;
import edu.cit.besanez.cookbook.recipe.RecipeRepository;
import edu.cit.besanez.cookbook.recipe.RecipeResponseDTO;
import edu.cit.besanez.cookbook.shared.exception.ResourceNotFoundException;
import edu.cit.besanez.cookbook.user.UserEntity;
import edu.cit.besanez.cookbook.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final InstructionRepository instructionRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    private static final String FRONTEND_BASE_URL = "http://localhost:3000";

    // ─── Generate / refresh share token ──────────────────────────────────────

    @Transactional
    public Map<String, String> generateShareToken(long userId, Long recipeId) {
        RecipeEntity recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Recipe not found with id: " + recipeId));

        if (recipe.getShareToken() == null) {
            recipe.setShareToken(UUID.randomUUID().toString());
            recipeRepository.save(recipe);
        }

        String shareUrl = FRONTEND_BASE_URL + "/shared/" + recipe.getShareToken();

        Map<String, String> result = new HashMap<>();
        result.put("shareToken", recipe.getShareToken());
        result.put("shareUrl", shareUrl);
        return result;
    }

    // ─── Revoke share token ───────────────────────────────────────────────────

    @Transactional
    public void revokeShareToken(long userId, Long recipeId) {
        RecipeEntity recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Recipe not found with id: " + recipeId));

        recipe.setShareToken(null);
        recipeRepository.save(recipe);
    }

    // ─── Resolve share token → recipe preview ─────────────────────────────────

    @Transactional(readOnly = true)
    public RecipeResponseDTO getRecipeByShareToken(String token) {
        RecipeEntity recipe = recipeRepository.findByShareToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shared recipe not found or link has expired."));

        return convertToResponseDTO(recipe);
    }

    // ─── Save shared recipe as a copy ────────────────────────────────────────

    @Transactional
    public RecipeResponseDTO saveSharedRecipe(long userId, String token, List<Long> collectionIds) {
        RecipeEntity source = recipeRepository.findByShareToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shared recipe not found or link has expired."));

        if (source.getUser().getId() == userId) {
            throw new IllegalArgumentException("You cannot save your own recipe.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        RecipeEntity copy = RecipeEntity.builder()
                .name(source.getName())
                .description(source.getDescription())
                .prepTimeMinutes(source.getPrepTimeMinutes())
                .cookTimeMinutes(source.getCookTimeMinutes())
                .totalTimeMinutes(source.getTotalTimeMinutes())
                .notes(source.getNotes())
                .imageUrl(source.getImageUrl())
                .isPublic(false)
                .user(user)
                .build();

        RecipeEntity savedCopy = recipeRepository.save(copy);

        List<IngredientEntity> ingredients = ingredientRepository.findByRecipeId(source.getId());
        for (IngredientEntity ing : ingredients) {
            IngredientEntity clonedIng = IngredientEntity.builder()
                    .name(ing.getName())
                    .quantity(ing.getQuantity())
                    .unit(ing.getUnit())
                    .notes(ing.getNotes())
                    .recipe(savedCopy)
                    .build();
            ingredientRepository.save(clonedIng);
        }

        List<InstructionEntity> instructions = instructionRepository
                .findByRecipeIdOrderByStepNumberAsc(source.getId());
        for (InstructionEntity inst : instructions) {
            InstructionEntity clonedInst = InstructionEntity.builder()
                    .stepNumber(inst.getStepNumber())
                    .description(inst.getDescription())
                    .recipe(savedCopy)
                    .build();
            instructionRepository.save(clonedInst);
        }

        for (Long colId : collectionIds) {
            collectionRepository.findByIdAndUserId(colId, userId).ifPresent(col -> {
                col.addRecipe(savedCopy);
                collectionRepository.save(col);
            });
        }

        return convertToResponseDTO(savedCopy);
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