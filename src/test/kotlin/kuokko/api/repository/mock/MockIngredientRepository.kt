package kuokko.api.repository.mock

import javax.inject.Singleton
import kuokko.api.model.Ingredient
import kuokko.api.repository.database.DatabaseIngredientRepository
import kuokko.api.repository.IngredientRepository
import io.micronaut.context.annotation.Replaces

@Singleton
@Replaces(DatabaseIngredientRepository::class)
class MockIngredientRepository: IngredientRepository {
    override fun search(name: String) = (1 .. 10).map { Ingredient("$it", "Ingredient $it") }
    override fun get(id: String) = Ingredient(id, "Ingredient $id")
    override fun insert(ingredient: Ingredient) = ingredient.copy(id = "1")
}

