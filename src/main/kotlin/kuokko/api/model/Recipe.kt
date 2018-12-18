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
    val name: String
)

data class Technique(
    val id: String? = null,
    val name: String
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

    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator::class, property="id")
    @JsonIdentityReference(alwaysAsId=true)
    val ingredient: Ingredient? = null,
    val portion: Float? = null,
    val description: String? = null,
    val time: String? = null,

    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator::class, property="id")
    @JsonIdentityReference(alwaysAsId=true)
    val technique: Technique? = null,
    val note: String? = null
)

data class RecipeSummary(
    val id: String? = null,
    val title: String
)

data class Recipe(
    val id: String? = null,
    val title: String,
    val cookTime: String? = null,
    val dificulty: String? = null,
    val language: String = "ES_es",
    val ingredients: List<Ingredient> = listOf(),
    val tools: List<Tool> = listOf(),
    val techniques: List<Technique> = listOf(),
    val method: List<Instruction> = listOf()
) {
    fun summary() = RecipeSummary(this.id, this.title)
}
