package kuokko.api.repository.mock

import javax.inject.Singleton
import kuokko.api.model.Recipe
import kuokko.api.repository.database.DatabaseRecipeRepository
import kuokko.api.repository.RecipeRepository
import io.micronaut.context.annotation.Replaces

@Singleton
@Replaces(DatabaseRecipeRepository::class)
class MockRecipeRepository: RecipeRepository {
    override fun search(): List<Recipe> = (1 .. 10).map { Recipe("$it", "Recipe $it") }
    override fun get(id: String) = Recipe(id, "Recipe $id")
    override fun insert(recipe: Recipe) = recipe.copy(id = "1")
}

