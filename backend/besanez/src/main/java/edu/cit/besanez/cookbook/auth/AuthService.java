package edu.cit.besanez.cookbook.auth;

import edu.cit.besanez.cookbook.admin.AdminService;
import edu.cit.besanez.cookbook.image.CloudinaryService;
import edu.cit.besanez.cookbook.shared.exception.ResourceNotFoundException;
import edu.cit.besanez.cookbook.shared.util.JwtUtil;
import edu.cit.besanez.cookbook.user.CookingLevel;
import edu.cit.besanez.cookbook.user.DefaultDataSeederService;
import edu.cit.besanez.cookbook.user.UserEntity;
import edu.cit.besanez.cookbook.user.UserRepository;
import edu.cit.besanez.cookbook.user.UserRequestDTO;
import edu.cit.besanez.cookbook.user.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CloudinaryService cloudinaryService;
    private final DefaultDataSeederService seederService;
    private final AdminService adminService;
    private final JavaMailSender mailSender;

    // ─── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public UserResponseDTO register(UserRequestDTO userRequestDTO) {
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userRequestDTO.getEmail());
        }

        String encryptedPassword = passwordEncoder.encode(userRequestDTO.getPassword());

        UserEntity userEntity = UserEntity.builder()
                .firstName(userRequestDTO.getFirstName())
                .lastName(userRequestDTO.getLastName())
                .birthdate(userRequestDTO.getBirthdate())
                .email(userRequestDTO.getEmail())
                .password(encryptedPassword)
                .profileImage(userRequestDTO.getProfileImage())
                .cookingLevel(userRequestDTO.getCookingLevel() != null
                        ? userRequestDTO.getCookingLevel()
                        : CookingLevel.BEGINNER)
                .build();

        UserEntity savedUser = userRepository.save(userEntity);
        seederService.seedDefaultData(savedUser);

        return convertToResponseDTO(savedUser);
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + loginRequest.getEmail()));

        if (user.getPassword() == null) {
            throw new IllegalArgumentException(
                    "This account uses Google Sign-In. Please log in with Google.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        adminService.cleanupUserDataIfAdmin(user.getId(), user.getRole());

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return buildLoginResponse(token, user);
    }

    // ─── Google OAuth2 ────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse loginOrRegisterGoogleUser(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String picture = oauth2User.getAttribute("picture");

        boolean[] isNewUser = { false };

        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            String persistedImage = uploadGoogleAvatar(null, picture);

            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .firstName(firstName != null ? firstName : "Google")
                    .lastName(lastName != null ? lastName : "User")
                    .password(null)
                    .birthdate(null)
                    .profileImage(persistedImage)
                    .cookingLevel(CookingLevel.BEGINNER)
                    .build();

            UserEntity saved = userRepository.save(newUser);
            isNewUser[0] = true;
            return saved;
        });

        if (isNewUser[0]) {
            seederService.seedDefaultData(user);
        }

        boolean storedIsGoogleUrl = user.getProfileImage() != null
                && user.getProfileImage().contains("googleusercontent.com");

        if (!isNewUser[0] && picture != null && (storedIsGoogleUrl || user.getProfileImage() == null)) {
            String cloudinaryUrl = uploadGoogleAvatar(user.getId(), picture);
            if (cloudinaryUrl != null) {
                user.setProfileImage(cloudinaryUrl);
                userRepository.save(user);
            }
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return buildLoginResponse(token, user);
    }

    @Transactional
    public LoginResponse loginOrRegisterMobileGoogleUser(
            String email, String firstName, String lastName, String picture) {

        boolean[] isNewUser = { false };

        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            String persistedImage = uploadGoogleAvatar(null, picture);

            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .firstName(firstName != null ? firstName : "Google")
                    .lastName(lastName != null ? lastName : "User")
                    .password(null)
                    .birthdate(null)
                    .profileImage(persistedImage)
                    .cookingLevel(CookingLevel.BEGINNER)
                    .build();

            UserEntity saved = userRepository.save(newUser);
            isNewUser[0] = true;
            return saved;
        });

        if (isNewUser[0]) {
            seederService.seedDefaultData(user);
        }

        boolean storedIsGoogleUrl = user.getProfileImage() != null
                && user.getProfileImage().contains("googleusercontent.com");

        if (!isNewUser[0] && picture != null && (storedIsGoogleUrl || user.getProfileImage() == null)) {
            String cloudinaryUrl = uploadGoogleAvatar(user.getId(), picture);
            if (cloudinaryUrl != null) {
                user.setProfileImage(cloudinaryUrl);
                userRepository.save(user);
            }
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return buildLoginResponse(token, user);
    }

    // ─── Password management ──────────────────────────────────────────────────

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        if (user.getPassword() == null) {
            throw new IllegalArgumentException(
                    "Google Sign-In accounts cannot change password here.");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(String email, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        if (user.getPassword() == null) {
            throw new IllegalArgumentException(
                    "Google Sign-In accounts cannot reset password here.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String uploadGoogleAvatar(Long userId, String picture) {
        if (picture == null || picture.isBlank())
            return null;
        try {
            String folder = userId != null
                    ? "users/" + userId + "/profiles"
                    : "users/profiles";
            return cloudinaryService.uploadImageFromUrl(picture, folder);
        } catch (Exception e) {
            System.err.println("Google avatar upload to Cloudinary failed: " + e.getMessage());
            return picture;
        }
    }

    // ─── Send verification code ───────────────────────────────────────────────

    @Transactional
    public void sendVerificationCode(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("CookBook — Verification Code");
        message.setText("Your verification code is: " + code + "\n\nExpires in 10 minutes.");
        mailSender.send(message);
    }

    // ─── Verify code ──────────────────────────────────────────────────────────

    @Transactional
    public void verifyCode(String email, String code) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        if (user.getVerificationCode() == null || user.getVerificationExpiry() == null) {
            throw new IllegalArgumentException("No verification code requested.");
        }
        if (LocalDateTime.now().isAfter(user.getVerificationExpiry())) {
            throw new IllegalArgumentException("Verification code has expired.");
        }
        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Incorrect verification code.");
        }

        user.setVerificationCode(null);
        user.setVerificationExpiry(null);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    // ─── Change password via code ─────────────────────────────────────────────

    @Transactional
    public void changePasswordWithCode(String email, String code, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));

        if (user.getVerificationCode() == null || user.getVerificationExpiry() == null) {
            throw new IllegalArgumentException("No verification code requested.");
        }
        if (LocalDateTime.now().isAfter(user.getVerificationExpiry())) {
            throw new IllegalArgumentException("Verification code has expired.");
        }
        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Incorrect verification code.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setVerificationExpiry(null);
        userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(String token, UserEntity user) {
        UserResponseDTO userDTO = convertToResponseDTO(user);
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .user(userDTO)
                .build();
    }

    private UserResponseDTO convertToResponseDTO(UserEntity userEntity) {
        return UserResponseDTO.builder()
                .userId(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .birthdate(userEntity.getBirthdate())
                .email(userEntity.getEmail())
                .profileImage(userEntity.getProfileImage())
                .cookingLevel(userEntity.getCookingLevel())
                .role(userEntity.getRole())
                .emailVerified(userEntity.isEmailVerified())
                .build();
    }
}