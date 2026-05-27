package edu.cit.besanez.cookbook.share;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.besanez.cookbook.recipe.RecipeResponseDTO;
import edu.cit.besanez.cookbook.shared.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class ShareController {

    private final ShareService shareService;
    private final JwtUtil jwtUtil;

    private long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }

    @PostMapping("/recipe/{recipeId}")
    public ResponseEntity<?> generateShareToken(
            HttpServletRequest request,
            @PathVariable Long recipeId) {
        long userId = extractUserId(request);
        Map<String, String> result = shareService.generateShareToken(userId, recipeId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/recipe/{recipeId}")
    public ResponseEntity<Void> revokeShareToken(
            HttpServletRequest request,
            @PathVariable Long recipeId) {
        long userId = extractUserId(request);
        shareService.revokeShareToken(userId, recipeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{token}")
    public ResponseEntity<RecipeResponseDTO> getSharedRecipe(@PathVariable String token) {
        RecipeResponseDTO recipe = shareService.getRecipeByShareToken(token);
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/{token}/save")
    public ResponseEntity<RecipeResponseDTO> saveSharedRecipe(
            HttpServletRequest request,
            @PathVariable String token,
            @RequestBody(required = false) Map<String, Object> body) {
        long userId = extractUserId(request);

        List<Long> safeIds = List.of();
        if (body != null && body.containsKey("collectionIds")) {
            Object raw = body.get("collectionIds");
            if (raw instanceof List<?> rawList) {
                safeIds = rawList.stream()
                        .filter(item -> item instanceof Number)
                        .map(item -> ((Number) item).longValue())
                        .toList();
            }
        }

        RecipeResponseDTO saved = shareService.saveSharedRecipe(userId, token, safeIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}