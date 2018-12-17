package kuokko.api

import javax.inject.Inject

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces

import kuokko.api.model.Recipe
import kuokko.api.repository.RecipeRepository

@Controller("/recipes")
class RecipeController(
    val recipeRepository: RecipeRepository
) {

    @Get("/")
    fun listRecipes() = recipeRepository.search().map(Recipe::summary)

    @Get("/{id}")
    fun retrieveById(id: String) = recipeRepository.get(id)

}
