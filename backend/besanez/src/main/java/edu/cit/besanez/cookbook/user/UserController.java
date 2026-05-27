package edu.cit.besanez.cookbook.user;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.besanez.cookbook.admin.AdminService;
import edu.cit.besanez.cookbook.image.CloudinaryService;
import edu.cit.besanez.cookbook.shared.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final JwtUtil jwtUtil;
    private final AdminService adminService;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }

    private String extractEmail(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractEmail(token);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "No token provided"));
            }

            String email = extractEmail(request);
            UserResponseDTO user = userService.getUserByEmail(email);

            adminService.cleanupUserDataIfAdmin(user.getUserId(), user.getRole().toString());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("birthdate", user.getBirthdate());
            response.put("profileImage", user.getProfileImage());
            response.put("cookingLevel", user.getCookingLevel());
            response.put("role", user.getRole());
            response.put("createdAt", user.getCreatedAt());
            response.put("emailVerified", user.isEmailVerified());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
    }

    @PatchMapping("/me/profile-image")
    public ResponseEntity<UserResponseDTO> updateProfileImage(
            HttpServletRequest request,
            @Valid @RequestBody ProfileImageRequestDTO dto) {
        long userId = extractUserId(request);
        UserResponseDTO updated = userService.updateProfileImage(userId, dto.getProfileImage());
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/me/profile-image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "No file provided."));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Only image files are accepted (JPEG, PNG, GIF, WEBP)."));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Image must be smaller than 5 MB."));
            }

            long userId = extractUserId(request);

            String folder = "users/" + userId + "/profiles";
            String cloudinaryUrl = cloudinaryService.uploadImage(file, folder);

            UserResponseDTO updated = userService.updateProfileImage(userId, cloudinaryUrl);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to upload image."));
        }
    }

    // ─── Admin / general endpoints ────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@RequestParam String email) {
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable long id,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO updatedUser = userService.updateUser(id, userRequestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}