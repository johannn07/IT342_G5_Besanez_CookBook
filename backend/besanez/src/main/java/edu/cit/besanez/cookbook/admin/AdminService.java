package edu.cit.besanez.cookbook.admin;

import edu.cit.besanez.cookbook.collection.CollectionRepository;
import edu.cit.besanez.cookbook.collection.CollectionResponseDTO;
import edu.cit.besanez.cookbook.collection.CollectionService;
import edu.cit.besanez.cookbook.recipe.RecipeEntity;
import edu.cit.besanez.cookbook.recipe.RecipeRepository;
import edu.cit.besanez.cookbook.recipe.RecipeResponseDTO;
import edu.cit.besanez.cookbook.shared.exception.ResourceNotFoundException;
import edu.cit.besanez.cookbook.user.DefaultDataSeederService;
import edu.cit.besanez.cookbook.user.UserEntity;
import edu.cit.besanez.cookbook.user.UserRepository;
import edu.cit.besanez.cookbook.user.UserRequestDTO;
import edu.cit.besanez.cookbook.user.UserResponseDTO;
import edu.cit.besanez.cookbook.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CollectionRepository collectionRepository;
    private final UserService userService;
    private final CollectionService collectionService;
    private final DefaultDataSeederService seederService;

    // ─── Dashboard Stats ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminStatsDTO getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        long totalUsers = userRepository.count();
        long totalRecipes = recipeRepository.count();
        long totalCollections = collectionRepository.count();
        long publicRecipes = recipeRepository.countByIsPublicTrue();
        long newUsersLast7Days = userRepository.countByCreatedAtAfter(sevenDaysAgo);
        long newUsersLast30Days = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long newRecipesLast7Days = recipeRepository.countByCreatedAtAfter(sevenDaysAgo);
        long newRecipesLast30Days = recipeRepository.countByCreatedAtAfter(thirtyDaysAgo);

        // Build daily chart data for last 30 days
        List<Map<String, Object>> userGrowth = buildDailyGrowthData(
                userRepository.findAllByCreatedAtAfter(thirtyDaysAgo));
        List<Map<String, Object>> recipeGrowth = buildRecipeDailyGrowthData(
                recipeRepository.findAllByCreatedAtAfter(thirtyDaysAgo));

        // Recent users (last 10)
        List<AdminStatsDTO.RecentUserDTO> recentUsers = userRepository
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toRecentUserDTO)
                .collect(Collectors.toList());

        // Recent recipes (last 10)
        List<AdminStatsDTO.RecentRecipeDTO> recentRecipes = recipeRepository
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toRecentRecipeDTO)
                .collect(Collectors.toList());

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalRecipes(totalRecipes)
                .totalCollections(totalCollections)
                .publicRecipes(publicRecipes)
                .privateRecipes(totalRecipes - publicRecipes)
                .newUsersLast7Days(newUsersLast7Days)
                .newUsersLast30Days(newUsersLast30Days)
                .newRecipesLast7Days(newRecipesLast7Days)
                .newRecipesLast30Days(newRecipesLast30Days)
                .userGrowthLast30Days(userGrowth)
                .recipeGrowthLast30Days(recipeGrowth)
                .recentUsers(recentUsers)
                .recentRecipes(recentRecipes)
                .build();
    }

    // ─── User Management ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userService::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> searchUsers(String query, Pageable pageable) {
        return userRepository
                .findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        query, query, query, pageable)
                .map(userService::convertToResponseDTO);
    }

    @Transactional
    public UserResponseDTO updateUser(Long userId, UserRequestDTO dto) {
        return userService.updateUser(userId, dto);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userService.deleteUser(userId);
    }

    @Transactional
    public UserResponseDTO toggleAdminRole(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String newRole = "ADMIN".equals(user.getRole()) ? "USER" : "ADMIN";
        user.setRole(newRole);

        if ("ADMIN".equals(newRole)) {
            collectionRepository.deleteByUserId(userId);
            recipeRepository.deleteByUserId(userId);
        } else {
            seederService.seedDefaultData(user);
        }

        return userService.convertToResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void cleanupUserDataIfAdmin(Long userId, String role) {
        if ("ADMIN".equals(role)) {
            boolean hasData = recipeRepository.existsByUserId(userId) ||
                    collectionRepository.existsByUserId(userId);
            if (hasData) {
                collectionRepository.deleteByUserId(userId);
                recipeRepository.deleteByUserId(userId);
            }
        }
    }

    // ─── Recipe Management ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<RecipeResponseDTO> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable).map(this::convertRecipe);
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponseDTO> searchRecipes(String query, Pageable pageable) {
        return recipeRepository.findByNameContainingIgnoreCase(query,
                pageable).map(this::convertRecipe);
    }

    @Transactional
    public void deleteRecipeAdmin(Long recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResourceNotFoundException("Recipe not found with id: " + recipeId);
        }
        recipeRepository.deleteById(recipeId);
    }

    // ─── Collection Management ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CollectionResponseDTO> getAllCollections(Pageable pageable) {
        return collectionRepository.findAll(pageable).map(collectionService::convertToResponseDTOPublic);
    }

    @Transactional
    public void deleteCollectionAdmin(Long collectionId) {
        if (!collectionRepository.existsById(collectionId)) {
            throw new ResourceNotFoundException("Collection not found with id: " + collectionId);
        }
        collectionRepository.deleteById(collectionId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<Map<String, Object>> buildDailyGrowthData(List<UserEntity> users) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> countsByDate = users.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().toLocalDate().format(fmt),
                        Collectors.counting()));
        return buildLast30DaysChart(countsByDate);
    }

    private List<Map<String, Object>> buildRecipeDailyGrowthData(List<RecipeEntity> recipes) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> countsByDate = recipes.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().toLocalDate().format(fmt),
                        Collectors.counting()));
        return buildLast30DaysChart(countsByDate);
    }

    private List<Map<String, Object>> buildLast30DaysChart(Map<String, Long> countsByDate) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            String date = today.minusDays(i).format(fmt);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date);
            point.put("count", countsByDate.getOrDefault(date, 0L));
            result.add(point);
        }
        return result;
    }

    private AdminStatsDTO.RecentUserDTO toRecentUserDTO(UserEntity u) {
        return AdminStatsDTO.RecentUserDTO.builder()
                .userId(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .profileImage(u.getProfileImage())
                .cookingLevel(u.getCookingLevel() != null ? u.getCookingLevel().name() : "BEGINNER")
                .joinedAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "")
                .build();
    }

    private AdminStatsDTO.RecentRecipeDTO toRecentRecipeDTO(RecipeEntity r) {
        return AdminStatsDTO.RecentRecipeDTO.builder()
                .recipeId(r.getId())
                .name(r.getName())
                .imageUrl(r.getImageUrl())
                .isPublic(r.isPublic())
                .ownerFirstName(r.getUser().getFirstName())
                .ownerLastName(r.getUser().getLastName())
                .ownerEmail(r.getUser().getEmail())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "")
                .build();
    }

    private RecipeResponseDTO convertRecipe(RecipeEntity recipe) {
        return RecipeResponseDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .totalTimeMinutes(recipe.getTotalTimeMinutes())
                .notes(recipe.getNotes())
                .imageUrl(recipe.getImageUrl())
                .isPublic(recipe.isPublic())
                .userId(recipe.getUser().getId())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }
}