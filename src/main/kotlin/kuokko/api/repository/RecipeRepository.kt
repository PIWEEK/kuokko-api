package kuokko.api.repository

import kuokko.api.model.Recipe

interface RecipeRepository {
    fun search(): List<Recipe>
    fun get(id: String): Recipe
}
