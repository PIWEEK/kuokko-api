package kuokko.api.repository

import kuokko.api.model.Recipe

interface RecipeRepository {
    fun search(title: String? = null, page: Int? = null): List<Recipe>
    fun get(id: String): Recipe?
    fun insert(recipe: Recipe): Recipe
}
