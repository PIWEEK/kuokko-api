package kuokko.api.model

import com.fasterxml.jackson.annotation.*

data class Ingredient(
    val id: String? = null,
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val preparation: String? = null
)

data class Tool(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val photo: String? = null
)

data class Technique(
    val id: String? = null,
    val name: String,
    val description: String? = null
)

data class Instruction(
    @JsonIgnore
    val id: String? = null,
    val description: String? = null,
    val steps: List<Step> = listOf()
)

data class Step(
    @JsonIgnore
    val id: String? = null,
    val action: String,

    // @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator::class, property="id")
    // @JsonIdentityReference(alwaysAsId=true)
    val ingredient: Ingredient? = null,
    val portion: Float? = null,
    val description: String? = null,
    val time: String? = null,

    // @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator::class, property="id")
    // @JsonIdentityReference(alwaysAsId=true)
    val technique: Technique? = null,
    val note: String? = null
)

data class RecipeSummary(
    val id: String? = null,
    val title: String,
    val author: String? = null,
    val photoUrl: String? = null
) {
    val _link: String
        get() = "http://localhost:8080/recipes/$id"
}

data class Recipe(
    val id: String? = null,
    val title: String,
    val author: String? = null,
    val cookTime: Int? = null,
    val preparationTime: Int? = null,
    val totalTime: Int? = null,
    val difficulty: String? = null,
    val language: String? = "ES_es",
    val photoUrl: String? = null,
    val recipeUrl: String? = null,
    val servings: Int? = null,

    val ingredients: List<Ingredient> = listOf(),
    val tools: List<Tool> = listOf(),
    val techniques: List<Technique> = listOf(),
    val method: List<Instruction> = listOf()
) {
    fun summary() = RecipeSummary(this.id, this.title, this.author, this.photoUrl)
}

data class RecipeFilter(
    val title: String? = null,
    val page: Int = 0
)
