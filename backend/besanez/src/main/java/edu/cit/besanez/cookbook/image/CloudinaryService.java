package edu.cit.besanez.cookbook.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image"));
        return (String) result.get("secure_url");
    }

    public String uploadImageFromUrl(String imageUrl, String folder) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                imageUrl,
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image"));
        return (String) result.get("secure_url");
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank())
            return;
        try {
            String publicId = imageUrl
                    .replaceAll("https://res.cloudinary.com/[^/]+/image/upload/v[0-9]+/", "")
                    .replaceAll("\\.[^.]+$", ""); // strip extension
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Failed to delete Cloudinary image: " + e.getMessage());
        }
    }
}