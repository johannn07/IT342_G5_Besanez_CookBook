package edu.cit.besanez.cookbook.recipe;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {
        Page<RecipeEntity> findByUserId(long userId, Pageable pageable);

        Optional<RecipeEntity> findByIdAndUserId(Long id, long userId);

        Page<RecipeEntity> findByUserIdAndNameContainingIgnoreCase(long userId, String name,
                        Pageable pageable);

        Page<RecipeEntity> findByIsPublicTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

        Optional<RecipeEntity> findByShareToken(String shareToken);

        boolean existsByUserId(long userId);

        Page<RecipeEntity> findByIsPublicTrue(Pageable pageable);

        boolean existsByIdAndUserId(Long id, long userId);

        @Query("""
                        SELECT r FROM RecipeEntity r
                        JOIN r.collections c
                        WHERE c.id = :collectionId
                        AND c.user.id = :userId
                        """)
        Page<RecipeEntity> findByCollectionIdAndUserId(@Param("collectionId") Long collectionId,
                        @Param("userId") long userId,
                        Pageable pageable);

        List<RecipeEntity> findByUserId(long userId);

        Page<RecipeEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

        long countByIsPublicTrue();

        long countByCreatedAtAfter(LocalDateTime dateTime);

        List<RecipeEntity> findAllByCreatedAtAfter(LocalDateTime dateTime);

        List<RecipeEntity> findTop10ByOrderByCreatedAtDesc();

        void deleteByUserId(long userId);
}