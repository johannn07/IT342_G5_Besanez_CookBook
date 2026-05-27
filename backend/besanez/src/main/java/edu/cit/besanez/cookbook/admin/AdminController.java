package edu.cit.besanez.cookbook.admin;

import edu.cit.besanez.cookbook.collection.CollectionResponseDTO;
import edu.cit.besanez.cookbook.recipe.RecipeResponseDTO;
import edu.cit.besanez.cookbook.user.UserRequestDTO;
import edu.cit.besanez.cookbook.user.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class AdminController {

    private final AdminService adminService;

    // ─── Dashboard ────────────────────────────────────────────────────────────

    /** GET /api/admin/stats */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    /** GET /api/admin/users?search=&page=&size= */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponseDTO> page = (search != null && !search.isBlank())
                ? adminService.searchUsers(search, pageable)
                : adminService.getAllUsers(pageable);
        return ResponseEntity.ok(page);
    }

    /** PUT /api/admin/users/:id */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(adminService.updateUser(id, dto));
    }

    /** DELETE /api/admin/users/:id */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/admin/users/:id/toggle-role — flip USER ↔ ADMIN */
    @PatchMapping("/users/{id}/toggle-role")
    public ResponseEntity<UserResponseDTO> toggleRole(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleAdminRole(id));
    }

    // ─── Recipes ──────────────────────────────────────────────────────────────

    /** GET /api/admin/recipes?search=&page=&size= */
    @GetMapping("/recipes")
    public ResponseEntity<Page<RecipeResponseDTO>> getRecipes(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RecipeResponseDTO> page = (search != null && !search.isBlank())
                ? adminService.searchRecipes(search, pageable)
                : adminService.getAllRecipes(pageable);
        return ResponseEntity.ok(page);
    }

    /** DELETE /api/admin/recipes/:id */
    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        adminService.deleteRecipeAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Collections ──────────────────────────────────────────────────────────

    /** GET /api/admin/collections?page=&size= */
    @GetMapping("/collections")
    public ResponseEntity<Page<CollectionResponseDTO>> getCollections(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllCollections(pageable));
    }

    /** DELETE /api/admin/collections/:id */
    @DeleteMapping("/collections/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        adminService.deleteCollectionAdmin(id);
        return ResponseEntity.noContent().build();
    }
}