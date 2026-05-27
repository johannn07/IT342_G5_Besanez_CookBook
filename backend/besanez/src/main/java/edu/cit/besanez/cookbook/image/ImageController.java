package edu.cit.besanez.cookbook.image;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.besanez.cookbook.shared.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class ImageController {

    private final CloudinaryService cloudinaryService;
    private final JwtUtil jwtUtil;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subfolder", defaultValue = "recipes") String subfolder) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "No file provided."));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only image files are accepted."));
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Image must be smaller than 5 MB."));
        }

        Long userId = extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Unauthorized: invalid or missing token."));
        }

        if (!"recipes".equals(subfolder) && !"profiles".equals(subfolder)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Subfolder must be 'recipes' or 'profiles'."));
        }

        String folder = "users/" + userId + "/" + subfolder;

        try {
            String url = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Upload failed: " + e.getMessage()));
        }
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractUserId(token);
        }
        return null;
    }
}