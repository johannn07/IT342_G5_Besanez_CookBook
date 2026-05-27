package edu.cit.besanez.cookbook.auth;

import edu.cit.besanez.cookbook.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;

    private UserResponseDTO user;
}