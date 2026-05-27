package edu.cit.besanez.cookbook.recipe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.besanez.cookbook.shared.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class RecipeController {

    private final RecipeService recipeService;
    private final JwtUtil jwtUtil;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /** POST /api/recipe */
    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            HttpServletRequest request,
            @Valid @RequestBody RecipeRequestDTO requestDTO) {
        long userId = extractUserId(request);
        RecipeResponseDTO created = recipeService.createRecipe(userId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<RecipeResponseDTO>> getRecipes(
            HttpServletRequest request,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long collection,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        long userId = extractUserId(request);

        Page<RecipeResponseDTO> recipes;

        if (search != null && !search.isBlank()) {
            recipes = recipeService.searchUserRecipes(userId, search, pageable);
        } else if (collection != null) {
            recipes = recipeService.getRecipesByCollection(userId, collection, pageable);
        } else {
            recipes = recipeService.getAllRecipesByUser(userId, pageable);
        }

        return ResponseEntity.ok(recipes);
    }

    /** GET /api/recipe/public?search=x — browse public recipes (paginated) */
    @GetMapping("/public")
    public ResponseEntity<Page<RecipeResponseDTO>> getPublicRecipes(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RecipeResponseDTO> recipes = recipeService.searchPublicRecipes(
                search != null ? search : "", pageable);
        return ResponseEntity.ok(recipes);
    }

    /** GET /api/recipe/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getRecipeById(
            HttpServletRequest request,
            @PathVariable Long id) {
        long userId = extractUserId(request);
        RecipeResponseDTO recipe = recipeService.getRecipeById(userId, id);
        return ResponseEntity.ok(recipe);
    }

    /** PUT /api/recipe/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequestDTO requestDTO) {
        long userId = extractUserId(request);
        RecipeResponseDTO updated = recipeService.updateRecipe(userId, id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /** DELETE /api/recipe/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            HttpServletRequest request,
            @PathVariable Long id) {
        long userId = extractUserId(request);
        recipeService.deleteRecipe(userId, id);
        return ResponseEntity.noContent().build();
    }
}