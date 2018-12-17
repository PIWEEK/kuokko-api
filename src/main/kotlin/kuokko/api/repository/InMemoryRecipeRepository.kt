package kuokko.api.repository

import javax.inject.Singleton
import kuokko.api.model.Recipe

@Singleton
class InMemoryRecipeRepository: RecipeRepository {
    override fun search(): List<Recipe> = (1 .. 10).map { Recipe("$it", "Recipe $it") }
    override fun get(id: String) = Recipe(id, "Recipe $id")
}

