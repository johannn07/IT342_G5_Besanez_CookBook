package edu.cit.besanez.cookbook.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImageRequestDTO {

    @Size(max = 1000, message = "Profile image URL must not exceed 1000 characters")
    private String profileImage;
}