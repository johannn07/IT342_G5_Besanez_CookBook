package edu.cit.besanez.cookbook.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import edu.cit.besanez.cookbook.collection.CollectionEntity;
import edu.cit.besanez.cookbook.recipe.RecipeEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = true)
    private LocalDate birthdate;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CookingLevel cookingLevel = CookingLevel.BEGINNER;

    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "verification_expiry")
    private LocalDateTime verificationExpiry;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RecipeEntity> recipes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CollectionEntity> collections = new ArrayList<>();

    @Transient
    public int getAge() {
        if (this.birthdate == null) {
            return 0;
        }
        return Period.between(this.birthdate, LocalDate.now()).getYears();
    }

    public void addRecipe(RecipeEntity recipe) {
        this.recipes.add(recipe);
        recipe.setUser(this);
    }

    public void removeRecipe(RecipeEntity recipe) {
        this.recipes.remove(recipe);
        recipe.setUser(null);
    }

    public void addCollection(CollectionEntity collection) {
        this.collections.add(collection);
        collection.setUser(this);
    }

    public void removeCollection(CollectionEntity collection) {
        this.collections.remove(collection);
        collection.setUser(null);
    }
}