package kuokko.api.model

enum class RecipeDificulty {
    EASY, MEDIUM, HARD
}

data class Ingredient(
    val id: String
)

data class Tool(
    val id: String
)

data class Technique(
    val id: String
)

data class Step(
    val id: String
)

data class RecipeSummary(
    val id: String,
    val title: String
)

data class Recipe(
    val id: String,
    val title: String,
    val cookTime: String? = null,
    val dificulty: RecipeDificulty = RecipeDificulty.MEDIUM,
    val language: String = "ES_es",
    val ingredients: List<Ingredient> = listOf(),
    val tools: List<Tool> = listOf(),
    val techniques: List<Technique> = listOf(),
    val method: List<Step> = listOf()
) {
    val summary: RecipeSummary
    get() = RecipeSummary(this.id, this.title)
}
