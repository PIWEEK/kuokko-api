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

    fun insertRecipe(recipe: RecipeYml): Recipe {
        // TODO: Search for duplicated ingredients to sum them up
        val ingredients = recipe
                .ingredients
                .map { Ingredient(name = it.first(), quantity = it.getOrNull(1), preparation = it.getOrNull(2)) }
                .map(ingredientRepository::insert)

        val tools = recipe.tools
                .map { Tool(name = it) }
                .map(toolRepository::insert)

        val techniques = recipe.techniques
                .map { Technique(name = it.first()) }
                .map(techniqueRepository::insert)

        val instructions = (recipe.instructions ?: listOf())
                .map {
                    val steps = (it.steps ?: listOf()).map { step ->
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
                    }.filter { it != null}.map { it!! }
                    Instruction(steps = steps, description = it.description)
                }

        val result = recipeRepository.insert(
            Recipe(
                title = recipe.title!!,
                cookTime = recipe.time?.cooking,
                dificulty = recipe.dificulty,
                language = recipe.language ?: "ES_es",
                ingredients = ingredients,
                tools = tools,
                techniques = techniques,
                method = instructions
            )
        )

        return result
    }
}

class TimeYml {
    var cooking: String? = null
    var preparation: String? = null
    var total: String? = null
}

class StepYml {
    var add: String? = null
    var wait: String? = null
    var technique: String? = null
    var other: String? = null
    var note: String? = null
    var tool: String? = null
}

class InstructionYml {
    var tool: String? = null
    var steps: List<StepYml>? = null
    var description: String? = null
}

class RecipeYml {
    var title: String? = null
    var author: String? = null
    var description: String? = null
    var time: TimeYml? = null

    var dificulty: String? = null
    var servings: Int? = null
    var language: String? = null
    var photo: String? = null
    var url: String? = null

    var instructions: List<InstructionYml>? = null
}

val RecipeYml.ingredients: List<List<String>> get() {
    if (instructions == null) {
        return listOf()
    }

    return instructions!!
            .flatMap { it.steps?.map(StepYml::add) ?: listOf() }
            .filter { it != null }
            .map { it!!.split(",").map(String::trim) }
}

val RecipeYml.tools: List<String> get() {
    if (instructions == null) {
        return listOf()
    }

    val stepTools =  instructions!!
            .flatMap { it.steps?.map(StepYml::tool) ?: listOf() }
            .filter { it != null }
            .map { it!! }

    val instructionTools = instructions!!
            .map(InstructionYml::tool)
            .filter { it != null }
            .map { it !! }

    return stepTools + instructionTools
}

val RecipeYml.techniques: List<List<String>> get() {
    if (instructions == null) {
        return listOf()
    }

    return instructions!!
            .flatMap { it.steps?.map(StepYml::technique) ?: listOf() }
            .filter { it != null }
            .map { it!!.split(",").map(String::trim) }
}
