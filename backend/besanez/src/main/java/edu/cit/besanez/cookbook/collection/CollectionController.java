package edu.cit.besanez.cookbook.collection;

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
@RequestMapping("/api/collection")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class CollectionController {

    private final CollectionService collectionService;
    private final JwtUtil jwtUtil;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /** POST /api/collection */
    @PostMapping
    public ResponseEntity<CollectionResponseDTO> createCollection(
            HttpServletRequest request,
            @Valid @RequestBody CollectionRequestDTO requestDTO) {
        long userId = extractUserId(request);
        CollectionResponseDTO created = collectionService.createCollection(userId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<CollectionResponseDTO>> getAllCollections(
            HttpServletRequest request,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        long userId = extractUserId(request);

        Page<CollectionResponseDTO> collections = (search != null && !search.isBlank())
                ? collectionService.searchCollections(userId, search, pageable)
                : collectionService.getAllCollectionsByUser(userId, pageable);

        return ResponseEntity.ok(collections);
    }

    /** GET /api/collection/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> getCollectionById(
            HttpServletRequest request,
            @PathVariable Long id) {
        long userId = extractUserId(request);
        CollectionResponseDTO collection = collectionService.getCollectionById(userId, id);
        return ResponseEntity.ok(collection);
    }

    /** PUT /api/collection/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> updateCollection(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody CollectionRequestDTO requestDTO) {
        long userId = extractUserId(request);
        CollectionResponseDTO updated = collectionService.updateCollection(userId, id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /** DELETE /api/collection/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(
            HttpServletRequest request,
            @PathVariable Long id) {
        long userId = extractUserId(request);
        collectionService.deleteCollection(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ─── Recipe membership ────────────────────────────────────────────────────

    /** POST /api/collection/{id}/recipe/{recipeId} */
    @PostMapping("/{id}/recipe/{recipeId}")
    public ResponseEntity<CollectionResponseDTO> addRecipeToCollection(
            HttpServletRequest request,
            @PathVariable Long id,
            @PathVariable Long recipeId) {
        long userId = extractUserId(request);
        CollectionResponseDTO updated = collectionService.addRecipeToCollection(userId, id, recipeId);
        return ResponseEntity.ok(updated);
    }

    /** DELETE /api/collection/{id}/recipe/{recipeId} */
    @DeleteMapping("/{id}/recipe/{recipeId}")
    public ResponseEntity<CollectionResponseDTO> removeRecipeFromCollection(
            HttpServletRequest request,
            @PathVariable Long id,
            @PathVariable Long recipeId) {
        long userId = extractUserId(request);
        CollectionResponseDTO updated = collectionService.removeRecipeFromCollection(userId, id, recipeId);
        return ResponseEntity.ok(updated);
    }
}