package kuokko.api.data

val RecipeYml.ingredients: List<List<String>> get() =
    instructions?.let {
        it.flatMap { it.steps?.map(StepYml::add) ?: listOf() }
          .filter { it != null }
          .map { it!!.split(",").map(String::trim) }
    } ?: listOf()


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
