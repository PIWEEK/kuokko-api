package kuokko.api.repository.database

import java.util.UUID
import javax.inject.Singleton
import kuokko.api.model.*

import kuokko.api.repository.RecipeRepository
import kuokko.api.repository.IngredientRepository
import kuokko.api.repository.TechniqueRepository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.EntityID
import kuokko.api.db.*

@Singleton
class DatabaseRecipeRepository(
    val ingredientRepository: IngredientRepository,
    val techniqueRepository: TechniqueRepository
): RecipeRepository {
    fun hydrateBasic(row: ResultRow) = Recipe(
        id = row[RecipeDB.id].toString(),
        title = row[RecipeDB.title],
        author = row[RecipeDB.author],
        cookTime = row[RecipeDB.cookTime],
        preparationTime = row[RecipeDB.preparationTime],
        totalTime = row[RecipeDB.totalTime],
        dificulty = row[RecipeDB.dificulty],
        language = row[RecipeDB.language],
        photoUrl = row[RecipeDB.photoUrl],
        recipeUrl = row[RecipeDB.recipeUrl]
    )

    fun hydrate(row: ResultRow) = hydrateBasic(row).copy(
        ingredients = (RecipeWithIngredients leftJoin IngredientDB).select {
            RecipeWithIngredients.recipeId eq row[RecipeDB.id]
        }.map { ingredientRow ->
            Ingredient(
                id = ingredientRow[IngredientDB.id].toString(),
                name = ingredientRow[IngredientDB.name],
                quantity = ingredientRow[RecipeWithIngredients.quantity],
                unit = ingredientRow[RecipeWithIngredients.unit],
                preparation = ingredientRow[RecipeWithIngredients.preparation]
            )
        },

        method = RecipeEntry.select {
            RecipeEntry.recipeId eq row[RecipeDB.id]
        }.map { entryRow ->
            Instruction(
                id = entryRow[RecipeEntry.id].toString(),
                description = entryRow[RecipeEntry.description],
                steps = (RecipeEntry leftJoin RecipeStep).selectAll().map { stepRow ->
                    Step(
                        id = stepRow[RecipeStep.id].toString(),
                        action = stepRow[RecipeStep.action],
                        ingredient = stepRow[RecipeStep.ingredient]?.value?.let{ ingredientRepository.get("$it") },
                        portion = stepRow[RecipeStep.portion],
                        description = stepRow[RecipeStep.description],
                        time = stepRow[RecipeStep.time],
                        technique = stepRow[RecipeStep.technique]?.value?.let { techniqueRepository.get("$it") },
                        note = stepRow[RecipeStep.note]
                    )
                }
           )
        }
    )

    fun byId(id: String): QueryOp = { RecipeDB.id eq UUID.fromString(id) }
    fun byTitle(recipe: Recipe): QueryOp = { RecipeDB.title eq recipe.title }

    override fun search(): List<Recipe> = transaction {
        RecipeDB.selectAll().map(::hydrateBasic)
    }

    override fun get(id: String) = transaction {
        RecipeDB.select(byId(id)).firstOrNull()?.let(::hydrate)
    }

    override fun insert(recipe: Recipe): Recipe = transaction {
        var result = RecipeDB.select(byTitle(recipe)).firstOrNull()?.let(::hydrate)

        if (result == null) {
            val recipeId = RecipeDB.insertAndGetId {
                it[title] = recipe.title
                it[author] = recipe.author
                it[cookTime] = recipe.cookTime
                it[preparationTime] = recipe.preparationTime
                it[totalTime] = recipe.totalTime
                it[dificulty] = recipe.dificulty
                it[language] = recipe.language
                it[photoUrl] = recipe.photoUrl
                it[recipeUrl] = recipe.recipeUrl
            }

            for(ingredient in recipe.ingredients.distinctBy { it.id }) {
                RecipeWithIngredients.insert {
                    it[this.recipeId] = recipeId
                    it[ingredientId] = EntityID(UUID.fromString(ingredient.id), IngredientDB)
                    it[quantity] = ingredient.quantity
                    it[unit] = ingredient.unit
                    it[preparation] = ingredient.preparation
                }
            }

            for(tool in recipe.tools.distinctBy { it.id }) {
                RecipeWithTools.insert {
                    it[this.recipeId] = recipeId
                    it[toolId] = EntityID(UUID.fromString(tool.id), ToolDB)
                }
            }

            for(technique in recipe.techniques.distinctBy { it.id }) {
                RecipeWithTechniques.insert {
                    it[this.recipeId] = recipeId
                    it[techniqueId] = EntityID(UUID.fromString(technique.id), TechniqueDB)
                }
            }

            for (instruction in recipe.method) {
                val entryId = RecipeEntry.insertAndGetId {
                    it[this.recipeId] = recipeId
                    it[description] = instruction.description
                }

                for (step in instruction.steps) {
                    RecipeStep.insert {
                        it[this.recipeEntryId] = entryId
                        it[action] = step.action
                        it[ingredient] = step.ingredient?.id?.let{
                            EntityID(UUID.fromString(it), IngredientDB)
                        }
                        it[technique] = step.technique?.id?.let {
                            EntityID(UUID.fromString(it), TechniqueDB)
                        }
                        it[portion] = step.portion
                        it[description] = step.description
                        it[time] = step.time
                        it[note] = step.note
                    }
                }
            }

            result = recipe.copy(id = recipeId.value.toString())
        }

        result!!
    }
}

