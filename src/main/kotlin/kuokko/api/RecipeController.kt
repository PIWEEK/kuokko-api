package kuokko.api

import javax.inject.Inject
import java.util.Optional

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.context.annotation.Property

import kuokko.api.model.Recipe
import kuokko.api.model.RecipeFilter
import kuokko.api.repository.RecipeRepository
import kuokko.api.data.RecipesLoader
import javax.annotation.Nullable

@Controller("/recipes")
class RecipeController(
    val recipeRepository: RecipeRepository,
    val recipesLoader: RecipesLoader,
    @Property(name="recipes.url") var url: String
) {

    @Get("/{?recipeFilter*}")
    fun listRecipes(@Nullable recipeFilter: RecipeFilter?) =
        recipeRepository.search(
            title = recipeFilter?.title,
            page = recipeFilter?.page
        ).map { it.summary(url) }

    @Get("/{id}")
    fun retrieveById(id: String) = recipeRepository.get(id)

}
