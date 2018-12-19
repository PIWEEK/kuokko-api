package kuokko.api.data

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

    var difficulty: String? = null
    var servings: Int? = null
    var language: String? = null
    var photo: String? = null
    var url: String? = null

    var instructions: List<InstructionYml>? = null
}
