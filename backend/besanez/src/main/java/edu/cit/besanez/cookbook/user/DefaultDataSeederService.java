package edu.cit.besanez.cookbook.user;

import edu.cit.besanez.cookbook.collection.CollectionEntity;
import edu.cit.besanez.cookbook.collection.CollectionRepository;
import edu.cit.besanez.cookbook.ingredient.IngredientEntity;
import edu.cit.besanez.cookbook.ingredient.IngredientRepository;
import edu.cit.besanez.cookbook.ingredient.IngredientUnit;
import edu.cit.besanez.cookbook.instruction.InstructionEntity;
import edu.cit.besanez.cookbook.instruction.InstructionRepository;
import edu.cit.besanez.cookbook.recipe.RecipeEntity;
import edu.cit.besanez.cookbook.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultDataSeederService {

        private final RecipeRepository recipeRepository;
        private final IngredientRepository ingredientRepository;
        private final InstructionRepository instructionRepository;
        private final CollectionRepository collectionRepository;

        @Transactional
        public void seedDefaultData(UserEntity user) {
                try {
                        RecipeEntity adobo = createChickenAdobo(user);
                        RecipeEntity sinigang = createPorkSinigang(user);
                        RecipeEntity lechon = createLechonKawali(user);

                        createFilipinoFavoritesCollection(user, List.of(adobo, sinigang, lechon));
                } catch (Exception ex) {
                        // Never block registration — log and continue.
                        log.error("Failed to seed default data for user {}: {}", user.getId(), ex.getMessage(), ex);
                }
        }

        @SuppressWarnings("null")
        private RecipeEntity createChickenAdobo(UserEntity user) {
                @SuppressWarnings("null")
                RecipeEntity recipe = recipeRepository.save(
                                RecipeEntity.builder()
                                                .name("Chicken Adobo")
                                                .imageUrl("https://res.cloudinary.com/ddpoiwler/image/upload/v1778103629/Chicken_Adobo_bdzw8k.jpg")
                                                .description(
                                                                "The quintessential Filipino comfort dish — chicken braised in vinegar, "
                                                                                + "soy sauce, garlic, and bay leaves until meltingly tender with a "
                                                                                + "glossy, savory-tangy sauce.")
                                                .prepTimeMinutes(15)
                                                .cookTimeMinutes(45)
                                                .totalTimeMinutes(60)
                                                .notes(
                                                                "For deeper flavour, marinate the chicken for at least 1 hour (or overnight) "
                                                                                + "before cooking. Leftovers taste even better the next day as the flavours meld.")
                                                .isPublic(true)
                                                .user(user)
                                                .build());

                // Ingredients
                List<IngredientEntity> ingredients = List.of(
                                ingredient("Chicken, cut into serving pieces", 1, IngredientUnit.KG, null, recipe),
                                ingredient("Soy sauce", 60, IngredientUnit.ML, null, recipe),
                                ingredient("White cane vinegar", 60, IngredientUnit.ML, null, recipe),
                                ingredient("Garlic cloves, crushed", 6, null, null, recipe),
                                ingredient("Bay leaves", 3, null, null, recipe),
                                ingredient("Whole black peppercorns", 1, IngredientUnit.TSP, null, recipe),
                                ingredient("Water", 120, IngredientUnit.ML, null, recipe),
                                ingredient("Cooking oil", 2, IngredientUnit.TBSP, null, recipe),
                                ingredient("Sugar", 1, IngredientUnit.TSP, "optional, to balance acidity", recipe));
                ingredientRepository.saveAll(ingredients);

                // Instructions
                List<InstructionEntity> steps = List.of(
                                instruction(1,
                                                "Combine the chicken pieces with soy sauce, vinegar, garlic, bay leaves, and peppercorns in a bowl. Toss well to coat, then marinate for at least 30 minutes at room temperature (or overnight in the fridge).",
                                                recipe),
                                instruction(2,
                                                "Heat the cooking oil in a wide, heavy-bottomed pot or wok over medium-high heat. Remove the chicken from the marinade (reserve the marinade) and sear in batches until golden brown on all sides, about 3–4 minutes per side. Set aside.",
                                                recipe),
                                instruction(3,
                                                "Pour the reserved marinade and the water into the pot. Bring to a boil, scraping up any browned bits from the bottom.",
                                                recipe),
                                instruction(4,
                                                "Return the seared chicken to the pot. Lower the heat to medium-low, cover, and simmer for 25–30 minutes, turning the chicken halfway through, until cooked through and tender.",
                                                recipe),
                                instruction(5,
                                                "Uncover and increase heat to medium-high. Continue cooking, stirring occasionally, until the sauce has reduced and thickened to a glossy glaze that coats the chicken, about 10 minutes. Taste and adjust with sugar if desired.",
                                                recipe),
                                instruction(6,
                                                "Discard the bay leaves. Transfer to a serving platter and serve hot over steamed white rice.",
                                                recipe));
                instructionRepository.saveAll(steps);

                return recipe;
        }

        @SuppressWarnings("null")
        private RecipeEntity createPorkSinigang(UserEntity user) {
                @SuppressWarnings("null")
                RecipeEntity recipe = recipeRepository.save(
                                RecipeEntity.builder()
                                                .name("Pork Sinigang")
                                                .imageUrl("https://res.cloudinary.com/ddpoiwler/image/upload/v1778103629/Pork_Sinigang_mhygzv.jpg")
                                                .description(
                                                                "A hearty Filipino sour soup made with tender pork ribs slow-simmered "
                                                                                + "in a tamarind broth and loaded with fresh vegetables. "
                                                                                + "Sour, savoury, and deeply satisfying.")
                                                .prepTimeMinutes(20)
                                                .cookTimeMinutes(60)
                                                .totalTimeMinutes(80)
                                                .notes(
                                                                "Sampalok (fresh tamarind) gives the best flavour, but tamarind soup base "
                                                                                + "powder is a perfectly good shortcut. Adjust the amount to your preferred "
                                                                                + "level of sourness. Do not add the kangkong until the last 2 minutes — "
                                                                                + "it wilts very quickly.")
                                                .isPublic(true)
                                                .user(user)
                                                .build());

                List<IngredientEntity> ingredients = List.of(
                                ingredient("Pork ribs or pork belly, cut into chunks", 1, IngredientUnit.KG, null,
                                                recipe),
                                ingredient("Water", 2, IngredientUnit.L, null, recipe),
                                ingredient("Tamarind soup base powder (sampalok mix)", 40, IngredientUnit.G,
                                                "or fresh tamarind to taste", recipe),
                                ingredient("Tomatoes, quartered", 2, null, "medium-sized", recipe),
                                ingredient("Onion, quartered", 1, null, "medium-sized", recipe),
                                ingredient("Daikon radish, sliced into rounds", 1, null, null, recipe),
                                ingredient("String beans (sitaw), cut into 2-inch pieces", 150, IngredientUnit.G, null,
                                                recipe),
                                ingredient("Eggplant, sliced", 1, null, "medium-sized", recipe),
                                ingredient("Kangkong (water spinach) leaves", 1, null, "bunch", recipe),
                                ingredient("Fish sauce (patis)", 2, IngredientUnit.TBSP, "to taste", recipe),
                                ingredient("Salt", 1, IngredientUnit.TSP, "to taste", recipe),
                                ingredient("Green finger chilli (siling pang-sigang)", 2, null,
                                                "whole, optional for heat", recipe));
                ingredientRepository.saveAll(ingredients);

                List<InstructionEntity> steps = List.of(
                                instruction(1,
                                                "Place the pork and water in a large pot. Bring to a boil over high heat. Skim off and discard all grey foam that rises to the surface.",
                                                recipe),
                                instruction(2,
                                                "Add the tomatoes and onion. Reduce heat to medium, cover, and simmer for 30–40 minutes until the pork is nearly tender.",
                                                recipe),
                                instruction(3, "Stir in the tamarind soup base powder. Add the daikon radish and cook for 5 minutes.",
                                                recipe),
                                instruction(4,
                                                "Add the eggplant, string beans, and whole chillies (if using). Continue simmering uncovered for another 5–7 minutes until the vegetables are just tender.",
                                                recipe),
                                instruction(5,
                                                "Season generously with fish sauce and salt. Taste and add more tamarind powder if you prefer a more sour broth.",
                                                recipe),
                                instruction(6,
                                                "Turn off the heat, add the kangkong leaves, and stir until wilted (about 1–2 minutes). Serve immediately with steamed rice and extra fish sauce on the side.",
                                                recipe));
                instructionRepository.saveAll(steps);

                return recipe;
        }

        @SuppressWarnings("null")
        private RecipeEntity createLechonKawali(UserEntity user) {
                @SuppressWarnings("null")
                RecipeEntity recipe = recipeRepository.save(
                                RecipeEntity.builder()
                                                .name("Lechon Kawali")
                                                .imageUrl("https://res.cloudinary.com/ddpoiwler/image/upload/v1778103629/Lechon_Kawali_uh6plz.jpg")
                                                .description(
                                                                "Crispy pan-fried pork belly — a Filipino street-food favourite. "
                                                                                + "The pork is boiled until tender, dried thoroughly, then deep-fried "
                                                                                + "to achieve shatteringly crisp skin and juicy, flavourful meat inside.")
                                                .prepTimeMinutes(10)
                                                .cookTimeMinutes(90)
                                                .totalTimeMinutes(100)
                                                .notes(
                                                                "The key to ultra-crispy skin is drying the boiled pork completely before frying. "
                                                                                + "For best results, refrigerate the boiled, dried pork uncovered overnight. "
                                                                                + "Serve with liver sauce (sarsa) or vinegar-garlic dipping sauce.")
                                                .isPublic(true)
                                                .user(user)
                                                .build());

                List<IngredientEntity> ingredients = List.of(
                                ingredient("Pork belly slab, skin-on", 1, IngredientUnit.KG, "about 3–4 cm thick",
                                                recipe),
                                ingredient("Water", 2, IngredientUnit.L, "enough to cover pork", recipe),
                                ingredient("Garlic cloves, crushed", 5, null, null, recipe),
                                ingredient("Bay leaves", 3, null, null, recipe),
                                ingredient("Whole black peppercorns", 1, IngredientUnit.TBSP, null, recipe),
                                ingredient("Salt", 2, IngredientUnit.TBSP, "for boiling water", recipe),
                                ingredient("Cooking oil", 1, IngredientUnit.L, "for deep frying", recipe),
                                ingredient("Salt", 1, IngredientUnit.TSP, "for rubbing skin before frying", recipe));
                ingredientRepository.saveAll(ingredients);

                List<InstructionEntity> steps = List.of(
                                instruction(1,
                                                "Place the pork belly in a large pot. Add water, garlic, bay leaves, peppercorns, and 2 tablespoons of salt. The water should fully cover the pork — add more if needed.",
                                                recipe),
                                instruction(2,
                                                "Bring to a boil over high heat, then reduce to a steady simmer. Cook uncovered for 45–60 minutes until the pork is fork-tender (a skewer should slide in and out easily) but the skin has not fallen apart.",
                                                recipe),
                                instruction(3,
                                                "Remove the pork and transfer to a wire rack. Pat the surface completely dry with paper towels, paying extra attention to the skin. Leave uncovered at room temperature for 30 minutes, or refrigerate overnight uncovered for crispier results.",
                                                recipe),
                                instruction(4,
                                                "Score the skin in a crosshatch pattern with a sharp knife (do not cut through to the meat). Rub 1 teaspoon of salt all over the skin.",
                                                recipe),
                                instruction(5,
                                                "Heat the cooking oil in a deep, heavy-bottomed kawali (wok) or pot to 180 °C (350 °F). Carefully lower the pork belly skin-side down into the hot oil using tongs. Stand back — it will spatter. Fry for 15–20 minutes, turning occasionally, until the skin is deep golden brown and blistered all over.",
                                                recipe),
                                instruction(6,
                                                "Remove and drain on a wire rack for 5 minutes (not paper towels, to preserve crispiness). Chop into bite-sized pieces using a heavy knife or cleaver. Serve immediately with your favourite dipping sauce and steamed rice.",
                                                recipe));
                instructionRepository.saveAll(steps);

                return recipe;
        }

        private void createFilipinoFavoritesCollection(UserEntity user, List<RecipeEntity> recipes) {
                CollectionEntity collection = CollectionEntity.builder()
                                .name("Filipino Favorites")
                                .description(
                                                "A starter collection of beloved Filipino classics — "
                                                                + "the everyday dishes that taste like home.")
                                .user(user)
                                .build();

                recipes.forEach(collection::addRecipe);
                collectionRepository.save(collection);
        }

        private IngredientEntity ingredient(String name, int qty, IngredientUnit unit,
                        String notes, RecipeEntity recipe) {
                return IngredientEntity.builder()
                                .name(name)
                                .quantity(qty)
                                .unit(unit)
                                .notes(notes)
                                .recipe(recipe)
                                .build();
        }

        private InstructionEntity instruction(int step, String description, RecipeEntity recipe) {
                return InstructionEntity.builder()
                                .stepNumber(step)
                                .description(description)
                                .recipe(recipe)
                                .build();
        }
}