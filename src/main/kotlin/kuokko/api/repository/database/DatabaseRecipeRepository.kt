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

class ILikeOp(expr1: Expression<*>, expr2: Expression<*>): ComparisonOp(expr1, expr2, "ilike")

@Singleton
class DatabaseRecipeRepository(
    val ingredientRepository: IngredientRepository,
    val techniqueRepository: TechniqueRepository
): RecipeRepository {
    fun hydrateBasic(row: ResultRow) = Recipe(
        id = row[RecipeDB.id].toString(),
        title = row[RecipeDB.title],
        author = row[RecipeDB.author],
        servings = row[RecipeDB.servings],
        cookTime = row[RecipeDB.cookTime],
        preparationTime = row[RecipeDB.preparationTime],
        totalTime = row[RecipeDB.totalTime],
        difficulty = row[RecipeDB.difficulty],
        language = row[RecipeDB.language],
        photoUrl = row[RecipeDB.photoUrl],
        recipeUrl = row[RecipeDB.recipeUrl]
    )

    fun hydrateIngredients(recipeId: EntityID<UUID>) = (RecipeWithIngredients leftJoin IngredientDB).select {
            RecipeWithIngredients.recipeId eq recipeId
        }.map { ingredientRow ->
            Ingredient(
                id = ingredientRow[IngredientDB.id].toString(),
                name = ingredientRow[IngredientDB.name],
                quantity = ingredientRow[RecipeWithIngredients.quantity],
                unit = ingredientRow[RecipeWithIngredients.unit],
                preparation = ingredientRow[RecipeWithIngredients.preparation]
            )
        }

    fun hydrateTools(recipeId: EntityID<UUID>) = (RecipeWithTools leftJoin ToolDB).select {
            RecipeWithTools.recipeId eq recipeId
        }.map { row ->
            Tool(
                id = row[ToolDB.id].toString(),
                name = row[ToolDB.name],
                description = row[ToolDB.description],
                photo = row[ToolDB.photo]
            )
        }

    fun hydrateTechniques(recipeId: EntityID<UUID>) = (RecipeWithTechniques leftJoin TechniqueDB).select {
            RecipeWithTechniques.recipeId eq recipeId
        }.map { row ->
            Technique(
                id = row[TechniqueDB.id].toString(),
                name = row[TechniqueDB.name],
                description = row[TechniqueDB.description]
            )
        }

    fun hydrateMethod(recipeId: EntityID<UUID>, ingredients: List<Ingredient>, techniques: List<Technique>) = RecipeEntry.select {
            RecipeEntry.recipeId eq recipeId
        }.map { entryRow ->
            val stepsResult = RecipeStep.select {
                RecipeStep.recipeEntryId eq entryRow[RecipeEntry.id]
            }.orderBy(RecipeStep.order to true)

            Instruction(
                id = entryRow[RecipeEntry.id].toString(),
                description = entryRow[RecipeEntry.description],
                steps = stepsResult.map { stepRow ->
                    Step(
                        id = stepRow[RecipeStep.id].toString(),
                        action = stepRow[RecipeStep.action],
                        ingredient = stepRow[RecipeStep.ingredient]?.value?.let{ id -> ingredients.find { it.id == "$id" } },
                        portion = stepRow[RecipeStep.portion],
                        description = stepRow[RecipeStep.description],
                        time = stepRow[RecipeStep.time],
                        technique = stepRow[RecipeStep.technique]?.value?.let { id -> techniques.find { it.id == "$id" } },
                        note = stepRow[RecipeStep.note]
                    )
                }
           )
        }

    fun hydrate(row: ResultRow) = row.let {
        val recipeId: EntityID<UUID> = it[RecipeDB.id]
        val ingredients = hydrateIngredients(recipeId)
        val techniques = hydrateTechniques(recipeId)
        val tools = hydrateTools(recipeId)
        val method = hydrateMethod(recipeId, ingredients, techniques)

        hydrateBasic(it).copy(
            ingredients = ingredients,
            techniques = techniques,
            tools = tools,
            method = method
        )
    }

    fun byId(id: String): QueryOp = { RecipeDB.id eq UUID.fromString(id) }
    fun byTitle(title: String): QueryOp = {
        title.split(" ").map(String::trim).fold(intLiteral(1) eq intLiteral(1)) { acc, v ->
            AndOp(acc, ILikeOp(RecipeDB.title, stringLiteral("%$v%")))
        }
    }

    override fun search(title: String?, page: Int?): List<Recipe> = transaction {
        // addLogger(StdOutSqlLogger)
        val query = when(title) {
            null -> RecipeDB.selectAll()
            else -> RecipeDB.select(byTitle(title))
        }
        query.limit(10, 10 * (page ?: 0)).map(::hydrateBasic)
    }

    override fun get(id: String) = transaction {
        RecipeDB.select(byId(id)).firstOrNull()?.let(::hydrate)
    }

    override fun insert(recipe: Recipe): Recipe = transaction {
        val recipeId = RecipeDB.insertAndGetId {
            it[title] = recipe.title
            it[author] = recipe.author
            it[servings] = recipe.servings
            it[cookTime] = recipe.cookTime
            it[preparationTime] = recipe.preparationTime
            it[totalTime] = recipe.totalTime
            it[difficulty] = recipe.difficulty
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

            var current: Int = 1
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
                    it[order] = current
                }
                current++
            }
        }

        recipe.copy(id = recipeId.value.toString())
    }
}

