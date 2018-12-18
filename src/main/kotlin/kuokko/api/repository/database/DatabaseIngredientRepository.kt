package kuokko.api.repository.database

import javax.inject.Singleton
import kuokko.api.model.Ingredient
import kuokko.api.repository.IngredientRepository

@Singleton
class DatabaseIngredientRepository: IngredientRepository {
    companion object {
        var counter: Int = 0
    }

    override fun search(name: String) = (1 .. 10).map { Ingredient("$it", "Ingredient $it") }
    override fun get(id: String) = Ingredient(id, "Ingredient $id")
    override fun insert(ingredient: Ingredient) = ingredient.copy(id = counter++.toString())
}

