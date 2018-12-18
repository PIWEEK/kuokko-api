package kuokko.api.repository

import kuokko.api.model.Ingredient

interface IngredientRepository {
    fun search(name: String): List<Ingredient>
    fun get(id: String): Ingredient?
    fun insert(ingredient: Ingredient): Ingredient
}
