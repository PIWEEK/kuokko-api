package kuokko.api

import javax.inject.Inject

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces

import kuokko.api.model.Recipe
import kuokko.api.repository.RecipeRepository
import kuokko.api.data.RecipesLoader

@Controller("/recipes")
class RecipeController(
    val recipeRepository: RecipeRepository,
    val recipesLoader: RecipesLoader
) {

    @Get("/")
    fun listRecipes() = recipeRepository.search().map(Recipe::summary)

    @Get("/{id}")
    fun retrieveById(id: String) = recipesLoader.processRecipes().getOrNull(0)

}
