package kuokko.api.data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Value
import io.micronaut.context.annotation.Property

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.stream.Collectors

import kuokko.api.repository.IngredientRepository
import kuokko.api.repository.RecipeRepository
import kuokko.api.repository.TechniqueRepository
import kuokko.api.repository.ToolRepository
import kuokko.api.db.DataSource

import kuokko.api.model.*

@Context
class RecipesLoader(
    var recipeRepository: RecipeRepository,
    var ingredientRepository: IngredientRepository,
    var techniqueRepository: TechniqueRepository,
    var toolRepository: ToolRepository,
    var dataSource: DataSource,
    @Property(name="recipes.data.folder") var recipesFolder: String
) {

    val objectMapper = ObjectMapper(YAMLFactory())

    init {
        objectMapper.registerModule(KotlinModule())
        processRecipes()
    }

    fun processRecipes(): List<Recipe> {
        val folderPath = Paths.get(recipesFolder)

        if (Files.exists(folderPath)) {
            return Files.find(folderPath, Int.MAX_VALUE, BiPredicate { path, attr -> shouldParse(path, attr)})
                .map(::parse)
                .filter { it != null }
                .map { it !! }
                .map(::insertRecipe)
                .collect(Collectors.toList());
        } else {
            return listOf()
        }
    }

    fun shouldParse(path: Path, attr: BasicFileAttributes): Boolean {
        return path.toString().endsWith("yml") && attr.isRegularFile()
    }

    fun parse(path: Path): RecipeYml? {
        try {
            return objectMapper.readValue(path.toFile(), RecipeYml::class.java)
        } catch (e: Exception) {
            println(e.message)
            return null
        }
    }

    fun parseIngredients(recipe: RecipeYml) = recipe
            .ingredients
            .map { Ingredient(name = it.first(), quantity = it.getOrNull(1), preparation = it.getOrNull(2)) }
            .map(ingredientRepository::insert)

    fun parseTools(recipe: RecipeYml) = recipe.tools
            .map { Tool(name = it) }
            .map(toolRepository::insert)

    fun parseTechniques(recipe: RecipeYml) = recipe.techniques
            .map { Technique(name = it.first()) }
            .map(techniqueRepository::insert)

    fun parseInstructions(ingredients: List<Ingredient>, techniques: List<Technique>, recipe: RecipeYml) = recipe.instructions?.map {
        Instruction(
            steps = parseSteps(ingredients, techniques, it),
            description = it.description
        )
    } ?: listOf()

    fun parseSteps(ingredients: List<Ingredient>, techniques: List<Technique>,
                   instruction: InstructionYml) = instruction.steps?.map { step ->
        when {
            step.add != null -> Step(
                action = "add",
                ingredient = ingredients.find { it.name == step.add!!.split(",").firstOrNull() },
                note = step.note
            )
            step.wait != null -> Step(
                action = "wait",
                time = step.wait,
                note = step.note
            )
            step.technique != null -> Step(
                action = "technique",
                technique = techniques.find { it.name == step.technique!!.split(",").firstOrNull() },
                note = step.note
            )
            step.other != null -> Step(
                action = "other",
                description = step.other,
                note = step.note
            )
            else -> null
        }
    }?.filter { it != null}?.map { it!! } ?: listOf()


    fun insertRecipe(recipe: RecipeYml) = recipeRepository.insert(
        Recipe(
            title = recipe.title!!,
            author = recipe.author,
            servings = recipe.servings?.toInt(),
            cookTime = recipe.time?.cooking?.toInt(),
            totalTime = recipe.time?.total?.toInt(),
            difficulty = recipe.difficulty,
            language = recipe.language ?: "ES_es",
            photoUrl = recipe.photo,
            ingredients = parseIngredients(recipe),
            tools = parseTools(recipe),
            techniques = parseTechniques(recipe)
        ).let {
            it.copy(method = parseInstructions(it.ingredients, it.techniques, recipe))
        }
    )

}
