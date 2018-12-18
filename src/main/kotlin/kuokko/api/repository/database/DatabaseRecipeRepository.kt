package kuokko.api.repository.database

import javax.inject.Singleton
import kuokko.api.model.Recipe
import kuokko.api.repository.RecipeRepository

@Singleton
class DatabaseRecipeRepository: RecipeRepository {
    companion object {
        var counter: Int = 0
    }

    override fun search(): List<Recipe> = (1 .. 10).map { Recipe("$it", "Recipe $it") }
    override fun get(id: String) = Recipe(id, "Recipe $id")
    override fun insert(recipe: Recipe) = recipe.copy(id = counter++.toString())
}

